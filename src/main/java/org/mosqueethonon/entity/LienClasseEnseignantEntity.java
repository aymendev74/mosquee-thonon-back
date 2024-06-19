package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "lien_classe_enseignant", schema = "moth")
@Data
public class LienClasseEnseignantEntity implements Auditable {

    @Id
    @Column(name = "idlice")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "idense")
    private EnseignantEntity idEnseignant;
    @Embedded
    private Signature signature;

}
