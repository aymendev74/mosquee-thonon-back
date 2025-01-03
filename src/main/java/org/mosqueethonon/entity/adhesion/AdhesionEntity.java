package org.mosqueethonon.entity.adhesion;

import lombok.Data;
import org.hibernate.annotations.Formula;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.v1.enums.StatutInscription;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "adhesion", schema = "moth")
@Data
public class AdhesionEntity implements Auditable {

    @Id
    @Column(name = "idadhe")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdadhetitre")
    private String titre;
    @Column(name = "txadhenom")
    private String nom;
    @Column(name = "txadheprenom")
    private String prenom;
    @Column(name = "dtadhenaissance")
    private LocalDate dateNaissance;
    @Column(name = "idtari")
    private Long idTarif;
    @Column(name = "txadhemobile")
    private String mobile;
    @Column(name = "txadheemail")
    private String email;
    @Column(name = "txadhenumrue")
    private String numeroEtRue;
    @Column(name = "noadhecodepostal")
    private Integer codePostal;
    @Column(name = "txadheville")
    private String ville;
    @Column(name = "mtadheautre")
    private BigDecimal montantAutre;
    @Column(name = "cdadhestatut")
    @Enumerated(EnumType.STRING)
    private StatutInscription statut;
    @Column(name = "dtadheinscription")
    private LocalDateTime dateInscription;
    @Formula("(select tari.mttari from moth.tarif tari where tari.idtari = idtari)")
    private BigDecimal montant;
    @Column(name = "noadhemembre")
    private Integer noMembre;
    @Embedded
    private Signature signature;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idadhe", updatable = false, insertable = false)
    private List<MailingConfirmationEntity> mailingConfirmations;

}
