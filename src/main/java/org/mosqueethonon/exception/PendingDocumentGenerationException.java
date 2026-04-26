package org.mosqueethonon.exception;

/**
 * Exception lancée lorsqu'une demande de mail est en cours de traitement mais qu'il existe un document
 * (pièce jointe) qui n'a pas encore été généré
 */
public class PendingDocumentGenerationException extends RuntimeException {

    public PendingDocumentGenerationException(String message) {
        super(message);
    }

}
