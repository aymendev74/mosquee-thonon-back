package org.mosqueethonon.entity.audit;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Data
public class Signature {

    @Column(name = "oh_date_cre")
    private LocalDateTime dateCreation;
    @Column(name = "oh_vis_cre")
    private String visaCreation;
    @Column(name = "oh_date_mod")
    private LocalDateTime dateModification;
    @Column(name = "oh_vis_mod")
    private String visaModification;


}
