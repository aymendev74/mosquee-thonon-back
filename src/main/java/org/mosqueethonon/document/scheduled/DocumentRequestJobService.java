package org.mosqueethonon.document.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.document.repository.DocumentRequestRepository;
import org.mosqueethonon.mail.service.MailRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentRequestJobService {

    private final DocumentRequestRepository documentRequestRepository;
    private final DocumentRequestProcessor documentRequestProcessor;
    private final MailRequestService mailRequestService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processNextPendingRequest() {
        Optional<DocumentRequestEntity> optionalRequest =
                documentRequestRepository.findFirstPendingWithLock();

        if (optionalRequest.isEmpty()) {
            return false;
        }

        DocumentRequestEntity request = optionalRequest.get();
        boolean completed = documentRequestProcessor.processDocumentRequest(request);
        if (completed) {
            mailRequestService.promoteReadyMailRequests(request.getId());
        }
        return true;
    }

}
