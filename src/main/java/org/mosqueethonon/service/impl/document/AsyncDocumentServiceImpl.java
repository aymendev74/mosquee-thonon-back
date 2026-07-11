package org.mosqueethonon.service.impl.document;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class AsyncDocumentServiceImpl implements AsyncDocumentService {

    private final DocumentRequestRepository documentRequestRepository;

    @Override
    @Transactional
    public DocumentRequestEntity requestDocumentGeneration(DocumentRequestType type, Long businessId) {
        return documentRequestRepository.findByTypeAndBusinessIdAndStatut(type, businessId, DocumentRequestStatut.PENDING)
                .map(existing -> {
                    log.info("Une demande de génération de document PENDING existe déjà pour le type {} et le business ID {}, réutilisation", type, businessId);
                    return existing;
                })
                .orElseGet(() -> {
                    DocumentRequestEntity request = new DocumentRequestEntity();
                    request.setType(type);
                    request.setBusinessId(businessId);
                    request.setStatut(DocumentRequestStatut.PENDING);
                    DocumentRequestEntity saved = documentRequestRepository.save(request);
                    log.info("Demande de génération de document créée pour le type {} et le business ID {}", type, businessId);
                    return saved;
                });
    }

}
