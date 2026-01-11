package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "inscription", schema = "moth")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cdinsctype", discriminatorType = DiscriminatorType.STRING)
@Data
public abstract class InscriptionEntity implements Auditable {

    @Id
    @Column(name = "idinsc")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdinscstatut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "dtinscinscription")
    private LocalDateTime dateInscription;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idresp", nullable = false)
    private ResponsableLegalEntity responsableLegal;
    @Column(name = "noinscinscription")
    private String noInscription;
    @Column(name = "noinscpositionattente")
    private Integer noPositionAttente;
    @Embedded
    private Signature signature;
    @Column(name = "mtinsctotal")
    private BigDecimal montantTotal;
    @Column(name = "cdinsctype", insertable = false, updatable = false)
    private String type;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idinsc", nullable = false)
    private List<EleveEntity> eleves;
    @Formula("(select moth.getAnneeScolaire(idinsc))")
    private String anneeScolaire;

}
