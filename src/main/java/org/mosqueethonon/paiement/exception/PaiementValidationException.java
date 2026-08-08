package org.mosqueethonon.paiement.exception;

import lombok.Getter;

/**
 * Violation d'une règle de saisie d'un paiement.
 * <p>
 * Porte un code stable, exploité par le front pour afficher un message compréhensible : les autres
 * exceptions de l'application se traduisent par une réponse au corps vide, ce qui ne permettrait
 * pas de distinguer « le montant dépasse le reste à payer » de « la date est dans le futur ».
 */
@Getter
public class PaiementValidationException extends RuntimeException {

    private final String code;

    public PaiementValidationException(PaiementErreurEnum erreur, String message) {
        super(message);
        this.code = erreur.name();
    }

}
