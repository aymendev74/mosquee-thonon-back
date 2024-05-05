package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private LocalDateTime dateInscription;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idresp", nullable = false)
    private ResponsableLegalEntity responsableLegal;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idinsc", nullable = false)
    private List<EleveEntity> eleves;
    @Column(name = "noinscinscription")
    private String noInscription;
    @Column(name = "noinscpositionattente")
    private Integer noPositionAttente;
    @Embedded
    private Signature signature;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idinsc", updatable = false, insertable = false)
    private List<MailingConfirmationEntity> mailingConfirmations;
    @Column(name = "txinscanneescolaire")
    private String anneeScolaire;
    @Column(name = "mtinsctotal")
    private BigDecimal montantTotal;

}
