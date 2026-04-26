package org.mosqueethonon.service.document;

import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestType;

public interface AsyncDocumentService {

    /**
     * Crée une demande de génération de document en statut PENDING.
     * Retourne l'entité persistée, ou null si un doublon PENDING existait déjà.
     */
    DocumentRequestEntity requestDocumentGeneration(DocumentRequestType type, Long businessId);

}
