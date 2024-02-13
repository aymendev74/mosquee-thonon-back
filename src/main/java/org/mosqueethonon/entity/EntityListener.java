package org.mosqueethonon.entity;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDate;

public class EntityListener {

    @PrePersist
    public void doBeforeInsert(Auditable auditable) {
        Signature signature = auditable.getSignature();
        if(signature == null) {
            signature = new Signature();
            auditable.setSignature(signature);
        }
        signature.setDateCreation(LocalDate.now());
        signature.setVisaCreation(getVisa());
    }

    @PreUpdate
    public void doBeforeUpdate(Auditable auditable) {
        Signature signature = auditable.getSignature();
        signature.setDateModification(LocalDate.now());
        signature.setVisaModification(getVisa());
    }

    private String getVisa() {
        if(SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof UtilisateurEntity) {
                return ((UtilisateurEntity) principal).getUsername();
            }
        }
        return "anonymous";
    }

}
