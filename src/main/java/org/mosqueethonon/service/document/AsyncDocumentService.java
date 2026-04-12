package org.mosqueethonon.service.document;

import org.mosqueethonon.enums.DocumentRequestType;

public interface AsyncDocumentService {

    <T> void requestDocumentGeneration(DocumentRequestType type, Long businessId);

}
