package org.mosqueethonon.paiement.repository;

import org.mosqueethonon.paiement.entity.PaiementEntity;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<PaiementEntity, Long> {

    /**
     * Les paiements annulés sont volontairement inclus : l'historique d'une inscription doit
     * montrer les corrections, pas les masquer.
     */
    List<PaiementEntity> findByTypeCibleAndIdCibleOrderByDatePaiementAscIdAsc(TypeCiblePaiementEnum typeCible,
                                                                             Long idCible);

    /**
     * Renvoie {@code null} en l'absence de paiement valide — le service le ramène à zéro. Un
     * {@code COALESCE} en JPQL imposerait de typer le littéral, ce que les dialectes traitent
     * différemment ; le faire en Java évite le sujet.
     */
    @Query("SELECT SUM(p.montant) FROM PaiementEntity p "
            + "WHERE p.typeCible = :typeCible "
            + "AND p.idCible = :idCible "
            + "AND p.statut = org.mosqueethonon.paiement.enums.StatutPaiementEnum.VALIDE")
    BigDecimal sumMontantValide(@Param("typeCible") TypeCiblePaiementEnum typeCible,
                                @Param("idCible") Long idCible);

}
