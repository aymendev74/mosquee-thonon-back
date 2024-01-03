package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "adhesion", schema = "moth")
@Data
public class AdhesionEntity implements Auditable {

    @Id
    @Column(name = "idadhe")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdadhetitre")
    private String titre;
    @Column(name = "txadhenom")
    private String nom;
    @Column(name = "txadheprenom")
    private String prenom;
    @Column(name = "dtadhenaissance")
    private LocalDate dateNaissance;
    @Column(name = "idtari")
    private Long idTarif;
    @Column(name = "txadhephone")
    private String telephone;
    @Column(name = "txadhemobile")
    private String mobile;
    @Column(name = "txadheemail")
    private String email;
    @Column(name = "txadhenumrue")
    private String numeroEtRue;
    @Column(name = "noadhecodepostal")
    private Integer codePostal;
    @Column(name = "txadheville")
    private String ville;
    @Column(name = "mtadheautre")
    private BigDecimal montantAutre;
    @Column(name = "cdadhestatut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "dtadheinscription")
    private LocalDate dateInscription;
    @Embedded
    private Signature signature;

}
