package org.mosqueethonon.service.mail;

public interface MailRequestService {

    /**
     * Après qu'un document est passé en COMPLETED, vérifie si des MailRequestEntity NOT_READY
     * liées à ce document peuvent désormais passer en PENDING (i.e. tous leurs documents sont COMPLETED).
     *
     * @param documentRequestId l'ID de la DocumentRequestEntity qui vient d'être complétée
     */
    void promoteReadyMailRequests(Long documentRequestId);

}
