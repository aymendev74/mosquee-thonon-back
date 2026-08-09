package org.mosqueethonon.paiement.enums;

/**
 * Moyen par lequel un règlement a été encaissé.
 * <p>
 * {@link #WEB} est réservé au règlement en ligne : il est déclaré dès maintenant pour figer le
 * contrat, mais refusé en saisie manuelle tant que le flux de paiement en ligne n'existe pas.
 */
public enum ModePaiementEnum {

    ESPECE,
    CARTE,
    CHEQUE,
    VIREMENT,
    WEB;

    /**
     * Un mode saisissable par un administrateur depuis l'application.
     */
    public boolean isSaisissableManuellement() {
        return this != WEB;
    }

}
