package org.mosqueethonon.security;

import org.mosqueethonon.entity.UtilisateurEntity;
import org.mosqueethonon.entity.UtilisateurRoleEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class SecurityContext {

    public boolean isAdmin() {
        if(SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null
        && SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    public String getVisa() {
        UtilisateurEntity user = this.getUser();
        if(user != null) {
            return user.getUsername();
        }
        return "anonymous";
    }

    private UtilisateurEntity getUser() {
        if(SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof UtilisateurEntity) {
                return ((UtilisateurEntity) principal);
            }
        }
        return null;
    }
}
