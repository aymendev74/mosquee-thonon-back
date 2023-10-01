package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "inscription", schema = "moth")
@Data
public class InscriptionEntity implements Auditable {

    @Id
    @Column(name = "idpers")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txpersnom")
    private String nom;
    @Column(name = "txpersprenom")
    private String prenom;
    @Column(name = "dtpersnaissance")
    private LocalDate dateNaissance;
    @Column(name = "txpersphone")
    private String telephone;
    @Column(name = "txpersemail")
    private String email;
    @Column(name = "cdperssexe")
    private String sexe;
    @Column(name = "txpersnumrue")
    private String numeroEtRue;
    @Column(name = "noperscodepostal")
    private Integer codePostal;
    @Column(name = "txpersville")
    private String ville;
    @Column(name = "cdpersstatut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Embedded
    private Signature signature;

}
