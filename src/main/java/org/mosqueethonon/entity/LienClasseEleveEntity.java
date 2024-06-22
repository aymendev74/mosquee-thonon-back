package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "lien_classe_eleve", schema = "moth")
@Data
public class LienClasseEleveEntity implements Auditable {

    @Id
    @Column(name = "idlcel")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "idelev")
    private EleveEntity eleve;
    @Embedded
    private Signature signature;

}
