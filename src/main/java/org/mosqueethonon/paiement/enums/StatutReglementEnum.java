package org.mosqueethonon.paiement.enums;

import java.math.BigDecimal;

/**
 * État de règlement d'un objet métier, déduit du montant dû et du montant encaissé.
 * <p>
 * Jamais persisté : le recalculer systématiquement garantit qu'il reste juste quand le tarif d'une
 * inscription évolue après un encaissement.
 */
public enum StatutReglementEnum {

    NON_REGLE,
    PARTIEL,
    SOLDE,
    TROP_PERCU;

    /**
     * {@link #TROP_PERCU} est testé avant {@link #SOLDE} : le sur-paiement étant refusé à la saisie,
     * il ne peut résulter que d'un tarif revu à la baisse après encaissement, et doit alors être
     * signalé plutôt que confondu avec une inscription soldée.
     */
    public static StatutReglementEnum of(BigDecimal montantTotal, BigDecimal montantRegle) {
        int comparaison = montantRegle.compareTo(montantTotal);
        if (comparaison > 0) {
            return TROP_PERCU;
        }
        if (comparaison == 0) {
            return SOLDE;
        }
        return montantRegle.signum() == 0 ? NON_REGLE : PARTIEL;
    }

}
