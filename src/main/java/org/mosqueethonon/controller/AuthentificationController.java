package org.mosqueethonon.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.configuration.security.AuthCookieProperties;
import org.mosqueethonon.service.auth.IAuthService;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class AuthentificationController {

    private IAuthService oauthService;

    private AuthCookieProperties authCookieProperties;

    @GetMapping("/login")
    public String login() {
        return "login";
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
                .secure(this.authCookieProperties.isSecure())
                .path(this.authCookieProperties.getPath())
                .sameSite(this.authCookieProperties.getSameSite())
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
