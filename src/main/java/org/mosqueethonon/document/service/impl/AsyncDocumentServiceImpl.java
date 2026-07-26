package org.mosqueethonon.document.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.document.enums.DocumentRequestStatutEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;
import org.mosqueethonon.document.repository.DocumentRequestRepository;
import org.mosqueethonon.document.service.AsyncDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class AsyncDocumentServiceImpl implements AsyncDocumentService {

    private final DocumentRequestRepository documentRequestRepository;

    @Override
    @Transactional
    public DocumentRequestEntity requestDocumentGeneration(DocumentRequestTypeEnum type, Long businessId) {
        return documentRequestRepository.findByTypeAndBusinessIdAndStatut(type, businessId, DocumentRequestStatutEnum.PENDING)
                .map(existing -> {
                    log.info("Une demande de génération de document PENDING existe déjà pour le type {} et le business ID {}, réutilisation", type, businessId);
                    return existing;
                })
                .orElseGet(() -> {
                    DocumentRequestEntity request = new DocumentRequestEntity();
                    request.setType(type);
                    request.setBusinessId(businessId);
                    request.setStatut(DocumentRequestStatutEnum.PENDING);
                    DocumentRequestEntity saved = documentRequestRepository.save(request);
                    log.info("Demande de génération de document créée pour le type {} et le business ID {}", type, businessId);
                    return saved;
                });
    }

}
