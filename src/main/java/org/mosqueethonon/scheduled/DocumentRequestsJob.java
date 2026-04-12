package org.mosqueethonon.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentRequestsJob {

    private final DocumentRequestRepository documentRequestRepository;
    private final DocumentRequestProcessor documentRequestProcessor;

    @Scheduled(fixedDelayString = "${scheduled.document-generation}", timeUnit = TimeUnit.MINUTES)
    public void processPendingDocumentRequests() {
        List<DocumentRequestEntity> requests = documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING);
        if (!CollectionUtils.isEmpty(requests)) {
            log.info("Il y a {} demandes de génération de documents à traiter", requests.size());
            requests.forEach(documentRequestProcessor::processDocumentRequest);
        }
    }

}
