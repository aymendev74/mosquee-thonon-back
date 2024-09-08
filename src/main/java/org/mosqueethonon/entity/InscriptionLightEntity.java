package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.enums.StatutInscription;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "v_inscription_light", schema = "moth")
@Data
public class InscriptionLightEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dateinscription")
    private LocalDateTime dateInscription;
    @Column(name = "idinscription")
    private Long idInscription;
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
    private String prenom;
    @Column(name = "nomresponsablelegal")
    private String nomResponsableLegal;
    @Column(name = "prenomresponsablelegal")
    private String prenomResponsableLegal;
    @Column(name = "nomcontacturgence")
    private String nomContactUrgence;
    @Column(name = "prenomcontacturgence")
    private String prenomContactUrgence;
    @Column(name = "datenaissance")
    private LocalDate dateNaissance;
    @Column(name = "niveau")
    @Enumerated(EnumType.STRING)
    private NiveauScolaireEnum niveau;
    @Enumerated(EnumType.STRING)
    @Column(name = "niveauinterne")
    private NiveauInterneEnum niveauInterne;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "autorisationautonomie")
    private Boolean autorisationAutonomie;
    @Column(name = "autorisationmedia")
    private Boolean autorisationMedia;
    @Column(name = "mobilecontacturgence")
    private String mobileContactUrgence;
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "ville")
    private String ville;
    @Column(name = "noinscription")
    private String noInscription;
    @Column(name = "idperiode")
    private Long idPeriode;
    @Column(name = "email")
    private String email;
    private String type;

}
