package org.mosqueethonon.v1.dto.account;

import lombok.Data;

@Data
public class EnableAccountDto {

    private String username;
    private String token;
    private String password;

}
