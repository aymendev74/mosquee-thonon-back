package org.mosqueethonon.authentication.user;

import lombok.Data;

@Data
public class AuthRequest {

    private String username;
    private String password;

}
