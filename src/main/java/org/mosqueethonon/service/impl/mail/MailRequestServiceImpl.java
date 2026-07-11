package org.mosqueethonon.service.impl.mail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.repository.MailRequestDocumentRequestRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.mail.MailRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MailRequestServiceImpl implements MailRequestService {

    private final MailRequestDocumentRequestRepository mailRequestDocumentRequestRepository;
    private final MailRequestRepository mailRequestRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void promoteReadyMailRequests(Long documentRequestId) {
        List<Long> readyMailRequestIds = mailRequestDocumentRequestRepository.findReadyMailRequestIds(
                documentRequestId,
                MailRequestStatut.NOT_READY.name(),
                DocumentRequestStatut.COMPLETED.name()
        );
        if (CollectionUtils.isEmpty(readyMailRequestIds)) {
            return;
        }

        log.info("{} demande(s) de mail peuvent passer en PENDING suite à la complétion du document {}",
                readyMailRequestIds.size(), documentRequestId);

        List<MailRequestEntity> mailRequests = mailRequestRepository.findAllById(readyMailRequestIds);
        List<MailRequestEntity> toPromote = mailRequests.stream()
                .filter(mr -> mr.getStatut() == MailRequestStatut.NOT_READY)
                .peek(mr -> mr.setStatut(MailRequestStatut.PENDING))
                .collect(Collectors.toList());

        if (!toPromote.isEmpty()) {
            mailRequestRepository.saveAll(toPromote);
            log.info("{} demande(s) de mail passée(s) en PENDING (tous les documents requis sont COMPLETED)", toPromote.size());
        }
    }

}
