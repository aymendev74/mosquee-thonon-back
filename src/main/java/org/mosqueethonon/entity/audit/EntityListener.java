package org.mosqueethonon.entity.audit;

import lombok.AllArgsConstructor;
import org.mosqueethonon.configuration.security.context.SecurityContext;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

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
