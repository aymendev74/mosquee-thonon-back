package org.mosqueethonon.service.impl.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.AuthCookieProperties;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.service.auth.IAuthService;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    @Value("${app.token-uri}")
    private String tokenUri;

    private final JwtDecoder jwtDecoder;

    private final SecurityContext securityContext;

    private final AuthCookieProperties authCookieProperties;

    @Override
    public Jwt exchangeCodeAgainstJWT(String code, String clientId, String redirectURI, String codeVerifier) {
        log.debug("Exchange code against JWT");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectURI);
        body.add("client_id", clientId);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUri, request, Map.class);

        log.debug("POST on token, status code: {}", tokenResponse.getStatusCode().value());

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        return jwtDecoder.decode(accessToken);
    }

    @Override
    public UserInfoDto getProfile() {
        return this.securityContext.getUserInfo();
    }

    @Override
    public void deleteTokenIfExpired(HttpServletResponse response, String accessToken) {
        try {
            jwtDecoder.decode(accessToken);
        } catch (JwtException e) {
            ResponseCookie deleteCookie = ResponseCookie.from("MOTH-TOKEN", "")
                    .httpOnly(true)
                    .secure(this.authCookieProperties.isSecure())
                    .path(this.authCookieProperties.getPath())
                    .sameSite(this.authCookieProperties.getSameSite())
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        }
    }
}
