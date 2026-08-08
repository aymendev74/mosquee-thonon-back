package org.mosqueethonon.paiement.service;

import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.mosqueethonon.paiement.v1.dto.PaiementDto;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;

/**
 * Gestion des règlements encaissés sur un objet métier.
 * <p>
 * Toutes les opérations renvoient la situation complète et non le seul paiement touché : c'est ce
 * dont l'appelant a besoin pour se réafficher.
 */
public interface PaiementService {

    SituationPaiementDto getSituation(TypeCiblePaiementEnum typeCible, Long idCible);

    SituationPaiementDto creer(PaiementDto paiement);

    SituationPaiementDto modifier(Long id, PaiementDto paiement);

    SituationPaiementDto annuler(Long id);

}
