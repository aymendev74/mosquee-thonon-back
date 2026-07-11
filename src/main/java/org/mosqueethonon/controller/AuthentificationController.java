package org.mosqueethonon.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.mosqueethonon.configuration.security.AuthCookieConfiguration;
import org.mosqueethonon.dto.auth.LoginRequestDto;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.service.auth.IAuthService;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthentificationController {

    private IAuthService oauthService;

    private AuthCookieConfiguration authCookieConfiguration;

    private AuthenticationManager authenticationManager;

    private SecurityContextRepository securityContextRepository;

    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Ensure a session exists, then rotate its ID to prevent session fixation
        // (mirrors Spring Security's default ChangeSessionIdAuthenticationStrategy,
        // which the formLogin filter chain applied automatically before this endpoint replaced it).
        httpRequest.getSession(true);
        httpRequest.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        userService.saveLoginHistory(request.getUsername());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/token")
    public ResponseEntity<UserInfoDto> handleAuthorizationCode(
            @RequestParam("code") String code,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectURI,
            @RequestParam("code_verifier") String codeVerifier) {

        Jwt jwt = oauthService.exchangeCodeAgainstJWT(code, clientId, redirectURI, codeVerifier);

        UserInfoDto userInfo = UserInfoDto.builder()
                .username(jwt.getSubject())
                .roles(jwt.getClaimAsStringList("roles"))
                .build();

        // Créer cookie sécurisé
        ResponseCookie cookie = ResponseCookie.from("MOTH-TOKEN", jwt.getTokenValue())
                .httpOnly(true)
                .secure(this.authCookieConfiguration.isSecure())
                .path(this.authCookieConfiguration.getPath())
                .sameSite(this.authCookieConfiguration.getSameSite())
                .build();

        // Retourner les infos nécessaires à l'UI (rôles, nom, etc.)
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(userInfo);
    }

    // endpoint "profile" qui renvoit le nom d'utilisateur et ses rôles
    @GetMapping("/profile")
    public ResponseEntity<UserInfoDto> getProfile() {
        return ResponseEntity.ok(oauthService.getProfile());
    }

}
