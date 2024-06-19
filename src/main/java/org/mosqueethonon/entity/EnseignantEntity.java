package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "enseignant", schema = "moth")
@Data
public class EnseignantEntity implements Auditable {

    @Id
    @Column(name = "idense")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txensenom")
    private String nom;
    @Column(name = "txenseprenom")
    private String prenom;
    @Column(name = "txensemobile")
    private String mobile;
    @Embedded
    private Signature signature;

}
