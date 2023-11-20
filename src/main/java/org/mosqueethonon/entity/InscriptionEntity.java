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
    @Column(name = "idinsc")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txinscnom")
    private String nom;
    @Column(name = "txinscprenom")
    private String prenom;
    @Column(name = "dtinscnaissance")
    private LocalDate dateNaissance;
    @Column(name = "txinscphone")
    private String telephone;
    @Column(name = "txinscemail")
    private String email;
    @Column(name = "cdinscsexe")
    private String sexe;
    @Column(name = "txinscnumrue")
    private String numeroEtRue;
    @Column(name = "noinsccodepostal")
    private Integer codePostal;
    @Column(name = "txinscville")
    private String ville;
    @Column(name = "cdinscstatut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "cdinscniveau")
    private String niveau;
    @Embedded
    private Signature signature;

}
