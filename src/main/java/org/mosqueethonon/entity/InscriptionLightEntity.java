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
    @Column(name = "datenaissance")
    private LocalDate dateNaissance;
    @Column(name = "niveau")
    @Enumerated(EnumType.STRING)
    private NiveauScolaireEnum niveau;
    @Enumerated(EnumType.STRING)
    @Column(name = "niveauinterne")
    private NiveauInterneEnum niveauInterne;
    @Column(name = "telephone")
    private String telephone;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "ville")
    private String ville;
    @Column(name = "noinscription")
    private String noInscription;
    @Column(name = "idperiode")
    private Long idPeriode;

}
