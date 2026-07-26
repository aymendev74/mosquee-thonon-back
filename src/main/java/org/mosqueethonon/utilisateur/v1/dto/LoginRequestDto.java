package org.mosqueethonon.utilisateur.v1.dto;

import lombok.Data;

@Data
public class LoginRequestDto {

    private String username;
    private String password;

}
