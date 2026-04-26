package org.mosqueethonon.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.repository.MailRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class MailRequestJobService {

    private final MailRequestRepository mailRequestRepository;
    private final MailRequestProcessor mailRequestProcessor;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processNextPendingRequest() {
        // On load et on verrouille le premier record pending à traiter
        Optional<MailRequestEntity> optionalRequest = mailRequestRepository.findFirstPendingWithLock();

        if (optionalRequest.isEmpty()) {
            return false;
        }

        // La query native de verouillage native précédente ne charge pas les associations — on les charge explicitement
        // avant de passer l'entité au processor pour éviter une LazyInitializationException.
        Long mailRequestId = optionalRequest.get().getId();
        optionalRequest = mailRequestRepository.findById(mailRequestId);

        if(optionalRequest.isEmpty()) {
            log.warn("Mail request {} not found after lock", mailRequestId);
            return true;
        }

        mailRequestProcessor.processMailRequest(optionalRequest.get());
        
        return true;
    }

}
