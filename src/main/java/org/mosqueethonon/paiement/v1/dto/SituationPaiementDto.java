package org.mosqueethonon.paiement.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.paiement.enums.StatutReglementEnum;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue complète du règlement d'un objet métier : ce qui est dû, ce qui a été encaissé, ce qui reste,
 * et le détail des opérations.
 * <p>
 * C'est le retour de toutes les opérations sur les paiements, y compris les mutations : toute
 * création, modification ou annulation change mécaniquement les montants, les renvoyer évite au
 * client un rechargement derrière chaque enregistrement.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SituationPaiementDto {

    private TypeCiblePaiementEnum typeCible;
    private Long idCible;
    private BigDecimal montantTotal;
    private BigDecimal montantRegle;
    private BigDecimal resteAPayer;
    private StatutReglementEnum statutReglement;
    /**
     * Historique complet, paiements annulés compris — ils sont affichés grisés côté front.
     */
    private List<PaiementDto> paiements;

}
