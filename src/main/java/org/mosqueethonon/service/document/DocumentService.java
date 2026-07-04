package org.mosqueethonon.service.document;

import org.mosqueethonon.entity.document.DocumentEntity;

public interface DocumentService {

    <T> DocumentEntity generateOrUpdateDocument(DocumentGenerator<T> generator, T entity);

    byte[] getDocumentContent(Long documentId);

    DocumentEntity findById(Long documentId);

    void deleteDocument(Long documentId);

}
