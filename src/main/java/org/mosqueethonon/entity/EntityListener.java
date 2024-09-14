package org.mosqueethonon.entity;

import lombok.AllArgsConstructor;
import org.mosqueethonon.security.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
public class EntityListener {

    private SecurityContext securityContext;

    @PrePersist
    public void doBeforeInsert(Auditable auditable) {
        Signature signature = auditable.getSignature();
        if(signature == null) {
            signature = new Signature();
            auditable.setSignature(signature);
        }
        signature.setDateCreation(LocalDateTime.now());
        signature.setVisaCreation(securityContext.getVisa());
    }

    @PreUpdate
    public void doBeforeUpdate(Auditable auditable) {
        Signature signature = auditable.getSignature();
        signature.setDateModification(LocalDateTime.now());
        signature.setVisaModification(securityContext.getVisa());
    }

}
