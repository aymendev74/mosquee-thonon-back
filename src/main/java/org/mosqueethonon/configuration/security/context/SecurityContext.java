package org.mosqueethonon.configuration.security.context;

import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityContext {

    public boolean isAdmin() {
        return hasRole(Roles.ROLE_ADMIN);
    }

    public boolean hasRole(String role) {
        if(SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null
        && SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(role));
        }
        return false;
    }

    public boolean hasAnyPrivilegedRole() {
        return hasRole(Roles.ROLE_ADMIN) || hasRole("ROLE_TRESORIER") || hasRole("ROLE_ENSEIGNANT");
    }

    public String getVisa() {
        String username = this.getUser();
        return username != null ? username : "anonymous";
    }

    public String getUser() {
        if(SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return null;
    }

}
