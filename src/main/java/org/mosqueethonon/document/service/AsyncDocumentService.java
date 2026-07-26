package org.mosqueethonon.document.service;

import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;

public interface AsyncDocumentService {

    /**
     * Crée une demande de génération de document en statut PENDING.
     * Retourne l'entité persistée, ou null si un doublon PENDING existait déjà.
     */
    DocumentRequestEntity requestDocumentGeneration(DocumentRequestTypeEnum type, Long businessId);

}
