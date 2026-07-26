package org.mosqueethonon.utilisateur.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.security.AuthCookieConfiguration;
import org.mosqueethonon.utilisateur.service.UserService;
import org.mosqueethonon.utilisateur.v1.dto.UserInfoDto;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestAuthServiceImpl {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserService userService;

    @Mock
    private AuthCookieConfiguration authCookieConfiguration;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void testGetProfile_WithAuthenticatedUser() {
        // Arrange
        UserInfoDto expectedUserInfo = UserInfoDto.builder()
                .username("testuser")
                .prenom("Jean")
                .roles(List.of("ROLE_UTILISATEUR"))
                .build();

        when(userService.getProfile()).thenReturn(expectedUserInfo);

        // Act
        UserInfoDto result = authService.getProfile();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Jean", result.getPrenom());
        assertEquals(1, result.getRoles().size());
        assertEquals("ROLE_UTILISATEUR", result.getRoles().get(0));

        verify(userService).getProfile();
    }

    @Test
    public void testGetProfile_WithAnonymousUser() {
        // Arrange
        when(userService.getProfile()).thenReturn(null);

        // Act
        UserInfoDto result = authService.getProfile();

        // Assert
        assertNull(result);
        verify(userService).getProfile();
    }

    @Test
    public void testNeTouchePasAuCookieQuandLeTokenEstValide() {
        // GIVEN
        when(jwtDecoder.decode("token-valide")).thenReturn(Mockito.mock(Jwt.class));

        // WHEN
        authService.deleteTokenIfExpired(response, "token-valide");

        // THEN
        verify(response, never()).addHeader(anyString(), anyString());
        verifyNoInteractions(authCookieConfiguration);
    }

    @Test
    public void testSupprimeLeCookieQuandLeTokenEstInvalide() {
        // GIVEN
        when(jwtDecoder.decode("token-expire")).thenThrow(new JwtException("expiré"));
        when(authCookieConfiguration.isSecure()).thenReturn(true);
        when(authCookieConfiguration.getPath()).thenReturn("/");
        when(authCookieConfiguration.getSameSite()).thenReturn("Strict");

        // WHEN
        authService.deleteTokenIfExpired(response, "token-expire");

        // THEN — un Set-Cookie de durée nulle, seul moyen d'effacer un cookie httpOnly côté navigateur
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), captor.capture());
        String cookie = captor.getValue();
        assertTrue(cookie.startsWith("MOTH-TOKEN="), "le cookie visé doit être MOTH-TOKEN : " + cookie);
        assertTrue(cookie.contains("Max-Age=0"), "le cookie doit expirer immédiatement : " + cookie);
        assertTrue(cookie.contains("HttpOnly"), cookie);
        assertTrue(cookie.contains("Secure"), cookie);
        assertTrue(cookie.contains("SameSite=Strict"), cookie);
        assertTrue(cookie.contains("Path=/"), cookie);
    }

    @Test
    public void testRespecteLaConfigurationNonSecuriseeDuCookie() {
        // GIVEN — hors HTTPS, le cookie ne doit pas porter l'attribut Secure
        when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("expiré"));
        when(authCookieConfiguration.isSecure()).thenReturn(false);
        when(authCookieConfiguration.getPath()).thenReturn("/api");
        when(authCookieConfiguration.getSameSite()).thenReturn("Lax");

        // WHEN
        authService.deleteTokenIfExpired(response, "peu-importe");

        // THEN
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), captor.capture());
        String cookie = captor.getValue();
        assertFalse(cookie.contains("Secure"), cookie);
        assertTrue(cookie.contains("Path=/api"), cookie);
        assertTrue(cookie.contains("SameSite=Lax"), cookie);
    }
}
