package org.mosqueethonon.paiement.enums;

/**
 * Cycle de vie d'un paiement.
 * <p>
 * Un paiement n'est jamais supprimé : une erreur de saisie se corrige en le passant à
 * {@link #ANNULE}, ce qui le sort du montant réglé tout en le laissant visible dans l'historique.
 * <p>
 * Le règlement en ligne ajoutera ici ses propres états (en attente, échoué, remboursé) sans
 * migration, la colonne étant un VARCHAR.
 */
public enum StatutPaiementEnum {

    VALIDE,
    ANNULE;

}
