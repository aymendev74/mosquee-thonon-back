package org.mosqueethonon.paiement.enums;

/**
 * Nature de l'objet métier réglé par un paiement.
 * <p>
 * Le couple (type, identifiant) est préféré à une clé étrangère pour que les paiements d'adhésion
 * ne coûtent qu'une valeur d'enum le jour où ils seront nécessaires. Même principe que
 * {@code document_request} et son couple {@code cddoretype} / {@code iddorebusi}.
 */
public enum TypeCiblePaiementEnum {

    INSCRIPTION;

}
