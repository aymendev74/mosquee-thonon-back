package org.mosqueethonon.v1.dto.user;

import lombok.Data;
import org.mosqueethonon.utils.StringUtils;

import java.util.Set;

@Data
public class UserDto {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private boolean enabled;
    private boolean locked;
    private String mobile;
    private String username;
    private Set<RoleDto> roles;

    public void normalize() {
        this.nom = StringUtils.normalize(nom);
        this.prenom = StringUtils.normalize(prenom);
        this.email = this.email.toLowerCase();
        this.username = this.username.toLowerCase();
    }
}
