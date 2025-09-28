package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.entity.referentiel.MatiereEntity;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "inscription_matiere", schema = "moth")
@Getter
@Setter
public class InscriptionMatiereEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idinma")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "idmati", nullable = false)
    private MatiereEntity matiere;
    private Signature signature;

}
