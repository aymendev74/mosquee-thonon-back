package org.mosqueethonon.service.impl.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestAuthServiceImpl {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserService userService;

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
}
