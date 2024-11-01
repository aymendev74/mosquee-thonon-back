package org.mosqueethonon.v1.dto.user;

import lombok.Data;

import java.util.Set;

@Data
public class UserDto {

    private String username;
    private String password;
    private Set<RoleDto> roles;

}
