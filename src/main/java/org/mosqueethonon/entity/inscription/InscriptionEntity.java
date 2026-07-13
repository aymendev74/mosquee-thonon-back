package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
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
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idresp", nullable = false)
    private ResponsableLegalEntity responsableLegal;
    @Column(name = "noinscinscription")
    private String noInscription;
    @Column(name = "noinscpositionattente")
    private Integer noPositionAttente;
    // NOT NULL DEFAULT false en base (cf. changelog 062) : garder ce défaut synchronisé.
    // Ne pas ajouter @Builder à cette hiérarchie sans @Builder.Default, sinon l'initialisation serait ignorée.
    @Column(name = "loinscreinscription")
    private Boolean reinscription = Boolean.FALSE;
    @Embedded
    private Signature signature;
    @Column(name = "mtinsctotal")
    private BigDecimal montantTotal;
    @Column(name = "idtari")
    private Long idTarif;
    @Column(name = "idutil")
    private Long idUtilisateur;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idutil", insertable = false, updatable = false)
    private UtilisateurEntity utilisateur;
    @Column(name = "cdinsctype", insertable = false, updatable = false)
    private String type;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idinsc", nullable = false)
    private List<EleveEntity> eleves;
    @Formula("(select moth.getAnneeScolaire(idinsc))")
    private String anneeScolaire;
    @Version
    @Column(name = "oh_version")
    private Long version;

}
