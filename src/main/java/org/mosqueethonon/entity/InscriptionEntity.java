package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "inscription", schema = "moth")
@Data
public class InscriptionEntity implements Auditable {

    @Id
    @Column(name = "idinsc")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdinscstatut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "dtinscinscription")
    private LocalDate dateInscription;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idresp", nullable = false)
    private ResponsableLegalEntity responsableLegal;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idinsc", nullable = false)
    private List<EleveEntity> eleves;
    @Column(name = "noinscinscription")
    private String noInscription;
    @Column(name = "noinscpositionattente")
    private Integer noPositionAttente;
    @Embedded
    private Signature signature;

}
