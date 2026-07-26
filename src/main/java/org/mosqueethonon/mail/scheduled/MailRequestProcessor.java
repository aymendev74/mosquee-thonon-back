package org.mosqueethonon.mail.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.common.security.ApplicationConfiguration;
import org.mosqueethonon.mail.dto.MailAttachmentDto;
import org.mosqueethonon.mail.dto.MailDto;
import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.mail.entity.MailRequestDocumentRequestEntity;
import org.mosqueethonon.mail.entity.MailRequestEntity;
import org.mosqueethonon.document.enums.DocumentRequestStatutEnum;
import org.mosqueethonon.mail.enums.MailRequestStatutEnum;
import org.mosqueethonon.mail.enums.MailRequestTypeEnum;
import org.mosqueethonon.document.exception.PendingDocumentGenerationException;
import org.mosqueethonon.document.repository.DocumentRequestRepository;
import org.mosqueethonon.mail.repository.MailRequestRepository;
import org.mosqueethonon.mail.service.impl.MailAdhesionServiceImpl;
import org.mosqueethonon.mail.service.impl.MailInscriptionServiceImpl;
import org.mosqueethonon.mail.service.MailService;
import org.mosqueethonon.param.service.ParamService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MailRequestProcessor {

    private final JavaMailSender emailSender;
    private final MailService mailInscriptionService;
    private final MailService mailAdhesionService;
    private final MailRequestRepository mailRequestRepository;
    private final DocumentRequestRepository documentRequestRepository;
    private final ParamService paramService;
    private final ApplicationConfiguration applicationConfiguration;

    public MailRequestProcessor(
            JavaMailSender emailSender,
            @Qualifier(MailInscriptionServiceImpl.MAIL_INSCRIPTION_SERVICE) MailService mailInscriptionService,
            @Qualifier(MailAdhesionServiceImpl.MAIL_ADHESION_SERVICE) MailService mailAdhesionService,
            MailRequestRepository mailRequestRepository,
            DocumentRequestRepository documentRequestRepository,
            ParamService paramService,
            ApplicationConfiguration applicationConfiguration) {
        this.emailSender = emailSender;
        this.mailInscriptionService = mailInscriptionService;
        this.mailAdhesionService = mailAdhesionService;
        this.mailRequestRepository = mailRequestRepository;
        this.documentRequestRepository = documentRequestRepository;
        this.paramService = paramService;
        this.applicationConfiguration = applicationConfiguration;
    }

    /**
     * Traite une demande d'envoi de mail en participant à la transaction appelante (REQUIRED).
     * Le lock SELECT FOR UPDATE acquis en amont par le service reste ainsi maintenu jusqu'au commit
     * de la transaction englobante, garantissant l'isolation en environnement multi-instances.
     */
    @Transactional
    public void processMailRequest(MailRequestEntity mailRequest) {
        try {
            if (!paramService.isSendEmailEnabled()) {
                log.info("Envoi de mail désactivé pour la demande {}", mailRequest.getId());
                mailRequest.setStatut(MailRequestStatutEnum.IGNORED);
                return;
            }

            MailDto mailDto = createMailDto(mailRequest);

            mailRequest.setSubject(mailDto.subject());
            mailRequest.setBody(mailDto.body());
            mailRequest.setAttachments(mailDto.attachments());

            log.info("Envoi du mail en cours pour la demande {}", mailRequest.getId());
            MimeMessage mimeMessage = createMimeMessage(mailDto);
            emailSender.send(mimeMessage);
            mailRequest.setStatut(MailRequestStatutEnum.SENT);
            log.info("Mail envoyé avec succès pour la demande {}", mailRequest.getId());

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la demande de mail {} : ", mailRequest.getId(), e);
            mailRequest.setStatut(MailRequestStatutEnum.ERROR);
        } finally {
            mailRequestRepository.save(mailRequest);
        }
    }

    private MailDto createMailDto(MailRequestEntity mailRequest) {
        MailDto mailDto;
        if (mailRequest.getType() == MailRequestTypeEnum.INSCRIPTION) {
            mailDto = mailInscriptionService.createMail(mailRequest.getBusinessId());
        } else if (mailRequest.getType() == MailRequestTypeEnum.ADHESION) {
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
        if (documents.stream().anyMatch(doc -> doc.getStatut() != DocumentRequestStatutEnum.COMPLETED)) {
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
        helper.setTo(mailDto.recipientEmail());
        helper.setSubject(mailDto.subject());
        helper.setText(mailDto.body(), true);
        if (mailDto.attachments() != null) {
            for (MailAttachmentDto attachment : mailDto.attachments()) {
                FileSystemResource file = new FileSystemResource(Paths.get(this.applicationConfiguration.getDocuments().getBasePath()).resolve(attachment.getLocation()));
                helper.addAttachment(attachment.getName(), file);
            }
        }
        return message;
    }
}
