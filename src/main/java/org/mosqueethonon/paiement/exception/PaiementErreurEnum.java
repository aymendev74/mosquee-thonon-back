package org.mosqueethonon.paiement.exception;

/**
 * Codes d'erreur de saisie d'un paiement, partagés avec le front qui les traduit en messages.
 * Toute modification d'un libellé de cet enum est un changement de contrat.
 */
public enum PaiementErreurEnum {

    CIBLE_INTROUVABLE,
    MONTANT_INVALIDE,
    MONTANT_SUPERIEUR_RESTE,
    DATE_OBLIGATOIRE,
    DATE_FUTURE,
    MODE_OBLIGATOIRE,
    MODE_WEB_NON_AUTORISE,
    PAIEMENT_ANNULE_NON_MODIFIABLE;

}
