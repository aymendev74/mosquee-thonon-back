package org.mosqueethonon.configuration.security.context;

import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
