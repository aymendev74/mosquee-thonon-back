package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.enums.StatutInscription;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "v_inscription_light", schema = "moth")
@Data
public class InscriptionLightEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dateinscription")
    private LocalDate dateInscription;
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
    @Column(name = "telephone")
    private String telephone;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "ville")
    private String ville;

}