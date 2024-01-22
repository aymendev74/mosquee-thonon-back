package org.mosqueethonon.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "tarif", schema = "moth")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @Column(name = "cdtaritarif")
    private String code;
    @Embedded
    private Signature signature;

}
