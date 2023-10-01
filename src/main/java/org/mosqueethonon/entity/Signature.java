package org.mosqueethonon.entity;

import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDate;

@Embeddable
@Data
public class Signature {

    @Column(name = "oh_date_cre")
    private LocalDate dateCreation;
    @Column(name = "oh_vis_cre")
    private String visaCreation;
    @Column(name = "oh_date_mod")
    private LocalDate dateModification;
    @Column(name = "oh_vis_mod")
    private String visaModification;


}
