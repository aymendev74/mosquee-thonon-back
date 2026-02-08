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
        if(SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null
        && SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Roles.ROLE_ADMIN));
        }
        return false;
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
