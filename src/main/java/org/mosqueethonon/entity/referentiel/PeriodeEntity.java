package org.mosqueethonon.entity.referentiel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "periode", schema = "moth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodeEntity implements Auditable {

    @Id
    @Column(name = "idperi")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dtperidebut")
    private LocalDate dateDebut;
    @Column(name = "dtperifin")
    private LocalDate dateFin;
    @Column(name = "noperianneedebut")
    private Integer anneeDebut;
    @Column(name = "noperianneefin")
    private Integer anneeFin;
    @Column(name = "nbperimaxinscription")
    private Integer nbMaxInscription;
    @Column(name = "cdperiapplication")
    private String application;
    @Column(name = "idperiprevious")
    private Long idPeriodePrecedente;
    @Embedded
    private Signature signature;

}
