package org.mosqueethonon.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String username;
    private String accessToken;

}
