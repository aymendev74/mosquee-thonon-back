package org.mosqueethonon.paiement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.common.audit.Auditable;
import org.mosqueethonon.common.audit.EntityListener;
import org.mosqueethonon.common.audit.Signature;
import org.mosqueethonon.paiement.enums.ModePaiementEnum;
import org.mosqueethonon.paiement.enums.StatutPaiementEnum;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un règlement encaissé sur un objet métier — aujourd'hui une inscription.
 * <p>
 * La cible est désignée par le couple {@code typeCible} / {@code idCible} plutôt que par une clé
 * étrangère : les inscriptions enfants et adultes partagent déjà la même table, et les adhésions
 * pourront être réglées sans changement de structure.
 */
@Entity
@EntityListeners(EntityListener.class)
@Table(name = "paiement", schema = "moth")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaiementEntity implements Auditable {

    @Id
    @Column(name = "idpaie")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdpaietypecible")
    @Enumerated(EnumType.STRING)
    private TypeCiblePaiementEnum typeCible;
    @Column(name = "idpaiecible")
    private Long idCible;
    @Column(name = "mtpaie")
    private BigDecimal montant;
    @Column(name = "dtpaie")
    private LocalDate datePaiement;
    @Column(name = "cdpaiemode")
    @Enumerated(EnumType.STRING)
    private ModePaiementEnum mode;
    @Column(name = "cdpaiestatut")
    @Enumerated(EnumType.STRING)
    private StatutPaiementEnum statut;
    /**
     * Référence externe du règlement : numéro de chèque aujourd'hui, identifiant de transaction du
     * prestataire de paiement en ligne demain. Les deux jouent le même rôle métier, une seule
     * colonne évite d'en ajouter une à chaque nouveau moyen de paiement.
     */
    @Column(name = "txpaiereference")
    private String reference;
    @Column(name = "txpaiecommentaire")
    private String commentaire;
    @Embedded
    private Signature signature;
    @Version
    @Column(name = "oh_version")
    private Long version;

}
