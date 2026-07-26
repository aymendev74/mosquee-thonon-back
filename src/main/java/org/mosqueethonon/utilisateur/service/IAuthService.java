package org.mosqueethonon.utilisateur.service;

import jakarta.servlet.http.HttpServletResponse;
import org.mosqueethonon.utilisateur.v1.dto.UserInfoDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface IAuthService {

    Jwt exchangeCodeAgainstJWT(String code, String clientId, String redirectURI, String codeVerifier);

    UserInfoDto getProfile();

    void deleteTokenIfExpired(HttpServletResponse response, String accessToken);

}
