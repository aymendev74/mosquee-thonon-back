package org.mosqueethonon.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.service.mail.MailRequestService;
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
