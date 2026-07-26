package org.mosqueethonon.utilisateur.v1.dto;

import lombok.Data;

@Data
public class EnableAccountDto {

    private String username;
    private String token;
    private String password;

}
