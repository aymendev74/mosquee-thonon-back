package org.mosqueethonon.mail.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.mail.entity.MailRequestEntity;
import org.mosqueethonon.document.enums.DocumentRequestStatutEnum;
import org.mosqueethonon.mail.enums.MailRequestStatutEnum;
import org.mosqueethonon.mail.repository.MailRequestDocumentRequestRepository;
import org.mosqueethonon.mail.repository.MailRequestRepository;
import org.mosqueethonon.mail.service.MailRequestService;
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
                MailRequestStatutEnum.NOT_READY.name(),
                DocumentRequestStatutEnum.COMPLETED.name()
        );
        if (CollectionUtils.isEmpty(readyMailRequestIds)) {
            return;
        }

        log.info("{} demande(s) de mail peuvent passer en PENDING suite à la complétion du document {}",
                readyMailRequestIds.size(), documentRequestId);

        List<MailRequestEntity> mailRequests = mailRequestRepository.findAllById(readyMailRequestIds);
        List<MailRequestEntity> toPromote = mailRequests.stream()
                .filter(mr -> mr.getStatut() == MailRequestStatutEnum.NOT_READY)
                .peek(mr -> mr.setStatut(MailRequestStatutEnum.PENDING))
                .collect(Collectors.toList());

        if (!toPromote.isEmpty()) {
            mailRequestRepository.saveAll(toPromote);
            log.info("{} demande(s) de mail passée(s) en PENDING (tous les documents requis sont COMPLETED)", toPromote.size());
        }
    }

}
