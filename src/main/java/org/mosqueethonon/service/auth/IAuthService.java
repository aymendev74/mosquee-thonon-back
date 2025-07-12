package org.mosqueethonon.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface IAuthService {

    Jwt exchangeCodeAgainstJWT(String code, String clientId, String redirectURI, String codeVerifier);

    UserInfoDto getProfile();

    void deleteTokenIfExpired(HttpServletResponse response, String accessToken);

}
