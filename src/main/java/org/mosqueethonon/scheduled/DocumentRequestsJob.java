package org.mosqueethonon.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.service.mail.MailRequestService;
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
    private final MailRequestService mailRequestService;

    @Scheduled(fixedDelayString = "${scheduled.document-generation}", timeUnit = TimeUnit.MINUTES)
    public void processPendingDocumentRequests() {
        List<DocumentRequestEntity> requests = documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING);
        if (!CollectionUtils.isEmpty(requests)) {
            log.info("Il y a {} demandes de génération de documents à traiter", requests.size());
            requests.forEach(this::processAndPromote);
        }
    }

    /**
     * Traite la demande de document (dans une transaction REQUIRES_NEW via le processeur),
     * puis, si le document est passé en COMPLETED, déclenche la promotion des mails NOT_READY
     * qui attendaient ce document. La promotion se fait après le commit de la transaction
     * du processeur, ce qui garantit que le statut COMPLETED est visible.
     */
    private void processAndPromote(DocumentRequestEntity request) {
        boolean completed = documentRequestProcessor.processDocumentRequest(request);
        if (completed) {
            mailRequestService.promoteReadyMailRequests(request.getId());
        }
    }

}
