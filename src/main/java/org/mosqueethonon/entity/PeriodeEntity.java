package org.mosqueethonon.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "periode", schema = "moth")
@Data
public class PeriodeEntity implements Auditable {

    @Id
    @Column(name = "idperi")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dtperidebut")
    private LocalDate dateDebut;
    @Column(name = "dtperifin")
    private LocalDate dateFin;
    @Column(name = "nbperimaxinscription")
    private Integer nbMaxInscription;
    @Column(name = "cdperiapplication")
    private String application;
    @Embedded
    private Signature signature;

}
