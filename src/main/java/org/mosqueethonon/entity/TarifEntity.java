package org.mosqueethonon.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "tarif", schema = "moth")
@Data
public class TarifEntity implements Auditable {

    @Id
    @Column(name = "idtari")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="idperi", nullable=false)
    private PeriodeEntity periode;
    @Column(name = "cdtariapplication")
    private String application;
    @Column(name = "cdtaritype")
    private String type;
    @Column(name = "lotariadherent")
    private Boolean adherent;
    @Column(name = "nbtarienfant")
    private Integer nbEnfant;
    @Column(name = "mttari")
    private BigDecimal montant;
    @Embedded
    private Signature signature;

}
