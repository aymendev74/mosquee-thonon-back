package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "v_adhesion_light", schema = "moth")
@Data
public class AdhesionLightEntity {

    @Column(name = "id")
    @Id
    private Long id;
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
    private String prenom;
    @Column(name = "ville")
    private String ville;
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "montant")
    private BigDecimal montant;
    @Column(name = "dateinscription")
    private LocalDate dateInscription;

}
