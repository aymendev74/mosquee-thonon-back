package org.mosqueethonon.entity.referentiel;

import lombok.Data;
import jakarta.persistence.*;
import org.mosqueethonon.entity.audit.Signature;

import java.time.LocalDate;

@Entity
@Table(name = "v_periode_cours", schema = "moth")
@Data
public class PeriodeInfoEntity {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "datedebut")
    private LocalDate dateDebut;
    @Column(name = "datefin")
    private LocalDate dateFin;
    @Column(name = "nbmaxinscription")
    private Integer nbMaxInscription;
    @Column(name = "existinscription")
    private Boolean existInscription;
    @Column(name = "active")
    private Boolean active;
    @Column(name = "application")
    private String application;
    @Embedded
    private Signature signature;

}
