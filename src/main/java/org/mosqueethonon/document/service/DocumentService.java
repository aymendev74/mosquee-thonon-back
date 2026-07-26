package org.mosqueethonon.document.service;

import org.mosqueethonon.document.entity.DocumentEntity;

public interface DocumentService {

    <T> DocumentEntity generateOrUpdateDocument(DocumentGenerator<T> generator, T entity);

    byte[] getDocumentContent(Long documentId);

    DocumentEntity findById(Long documentId);

    void deleteDocument(Long documentId);

}
