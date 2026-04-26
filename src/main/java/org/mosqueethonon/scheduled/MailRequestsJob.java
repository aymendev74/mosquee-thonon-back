package org.mosqueethonon.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.dto.mail.MailAttachmentDto;
import org.mosqueethonon.dto.mail.MailDto;
import org.mosqueethonon.entity.mail.MailRequestDocumentRequestEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.exception.PendingDocumentGenerationException;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.impl.mail.MailAdhesionServiceImpl;
import org.mosqueethonon.service.impl.mail.MailInscriptionServiceImpl;
import org.mosqueethonon.service.mail.MailService;
import org.mosqueethonon.service.param.ParamService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MailRequestsJob {

    private final JavaMailSender emailSender;
    private final MailService mailInscriptionService;
    private final MailService mailAdhesionService;
    private final MailRequestRepository mailRequestRepository;
    private final DocumentRequestRepository documentRequestRepository;
    private final ParamService paramService;

    public MailRequestsJob(
            JavaMailSender emailSender,
            @Qualifier(MailInscriptionServiceImpl.MAIL_INSCRIPTION_SERVICE) MailService mailInscriptionService,
            @Qualifier(MailAdhesionServiceImpl.MAIL_ADHESION_SERVICE) MailService mailAdhesionService,
            MailRequestRepository mailRequestRepository,
            DocumentRequestRepository documentRequestRepository,
            ParamService paramService) {
        this.emailSender = emailSender;
        this.mailInscriptionService = mailInscriptionService;
        this.mailAdhesionService = mailAdhesionService;
        this.mailRequestRepository = mailRequestRepository;
        this.documentRequestRepository = documentRequestRepository;
        this.paramService = paramService;
    }

    @Scheduled(fixedDelayString = "${scheduled.confirmation-mail}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void sendPendingEmails() {
        List<MailRequestEntity> mailRequests = mailRequestRepository.findByStatutWithDocumentsOrderBySignatureDateCreationAsc(MailRequestStatut.PENDING);
        if (!CollectionUtils.isEmpty(mailRequests)) {
            log.info("Il y a {} demandes d'envoi de mails à traiter", mailRequests.size());
            mailRequests.forEach(this::processMailRequest);
        }
    }

    private void processMailRequest(MailRequestEntity mailRequest) {
        try {
            if (!paramService.isSendEmailEnabled()) {
                log.info("Envoi de mail désactivé pour la demande {}", mailRequest.getId());
                mailRequest.setStatut(MailRequestStatut.IGNORED);
                return;
            }

            MailDto mailDto = createMailDto(mailRequest);

            mailRequest.setSubject(mailDto.getSubject());
            mailRequest.setBody(mailDto.getBody());
            mailRequest.setAttachments(mailDto.getAttachments());

            log.info("Envoi du mail en cours pour la demande {}", mailRequest.getId());
            MimeMessage mimeMessage = createMimeMessage(mailDto);
            emailSender.send(mimeMessage);
            mailRequest.setStatut(MailRequestStatut.SENT);
            log.info("Mail envoyé avec succès pour la demande {}", mailRequest.getId());

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la demande de mail {} : ", mailRequest.getId(), e);
            mailRequest.setStatut(MailRequestStatut.ERROR);
        } finally {
            mailRequestRepository.save(mailRequest);
        }
    }

    private MailDto createMailDto(MailRequestEntity mailRequest) {
        MailDto mailDto;
        if (mailRequest.getType() == MailRequestType.INSCRIPTION) {
            mailDto = mailInscriptionService.createMail(mailRequest.getBusinessId());
        } else if (mailRequest.getType() == MailRequestType.ADHESION) {
            mailDto = mailAdhesionService.createMail(mailRequest.getBusinessId());
        } else {
            throw new IllegalStateException("Type de demande de mail non géré : " + mailRequest.getType());
        }
        enrichWithGeneratedDocuments(mailRequest, mailDto);
        return mailDto;
    }

    private void enrichWithGeneratedDocuments(MailRequestEntity mailRequest, MailDto mailDto) {
        if (CollectionUtils.isEmpty(mailRequest.getDocumentRequests())) {
            return;
        }
        List<Long> documentIds = mailRequest.getDocumentRequests().stream()
                .map(MailRequestDocumentRequestEntity::getDocumentRequestId)
                .collect(Collectors.toList());
        List<DocumentRequestEntity> documents = documentRequestRepository.findAllById(documentIds);
        if (documents.stream().anyMatch(doc -> doc.getStatut() != DocumentRequestStatut.COMPLETED)) {
            throw new PendingDocumentGenerationException("Le mail ne peut pas être envoyé car au moins une pièce jointe n'a pas encore été générée - mailRequest : " + mailRequest.getId());
        }
        List<MailAttachmentDto> generatedAttachments = documents.stream()
                .map(doc -> MailAttachmentDto.builder()
                        .name(Paths.get(doc.getDocumentPath()).getFileName().toString())
                        .location(doc.getDocumentPath())
                        .build())
                .collect(Collectors.toList());
        if (!generatedAttachments.isEmpty()) {
            mailDto.addAttachments(generatedAttachments);
        }
    }

    private MimeMessage createMimeMessage(MailDto mailDto) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(mailDto.getRecipientEmail());
        helper.setSubject(mailDto.getSubject());
        helper.setText(mailDto.getBody(), true);
        if (mailDto.getAttachments() != null) {
            for (MailAttachmentDto attachment : mailDto.getAttachments()) {
                FileSystemResource file = new FileSystemResource(new File(attachment.getLocation()));
                helper.addAttachment(attachment.getName(), file);
            }
        }
        return message;
    }
}
