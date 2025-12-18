package org.mosqueethonon.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.dto.mail.MailAttachmentDto;
import org.mosqueethonon.dto.mail.MailDto;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;
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
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MailRequestsJob {

    private final JavaMailSender emailSender;
    private final MailService mailInscriptionService;
    private final MailService mailAdhesionService;
    private final MailRequestRepository mailRequestRepository;
    private final ParamService paramService;
    
    public MailRequestsJob(
            JavaMailSender emailSender,
            @Qualifier(MailInscriptionServiceImpl.MAIL_INSCRIPTION_SERVICE) MailService mailInscriptionService,
            @Qualifier(MailAdhesionServiceImpl.MAIL_ADHESION_SERVICE) MailService mailAdhesionService,
            MailRequestRepository mailRequestRepository,
            ParamService paramService) {
        this.emailSender = emailSender;
        this.mailInscriptionService = mailInscriptionService;
        this.mailAdhesionService = mailAdhesionService;
        this.mailRequestRepository = mailRequestRepository;
        this.paramService = paramService;
    }

    @Scheduled(fixedDelayString = "${scheduled.confirmation-mail}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void sendPendingEmails() {
        List<MailRequestEntity> mailRequests = mailRequestRepository.findByStatut(MailRequestStatut.PENDING);
        if (!CollectionUtils.isEmpty(mailRequests)) {
            log.info("Il y a {} demandes d'envoi de mails à traiter", mailRequests.size());
            mailRequests.forEach(this::processMailRequest);
        }
    }

    private void processMailRequest(MailRequestEntity mailRequest) {
        try {
            // Vérifier d'abord si l'envoi de mail est activé
            if (!paramService.isSendEmailEnabled()) {
                log.info("Envoi de mail désactivé pour la demande {}", mailRequest.getId());
                mailRequest.setStatut(MailRequestStatut.IGNORED);
                return;
            }

            // Si on arrive ici, l'envoi de mail est activé, on peut construire le mail
            MailDto mailDto = createMailDto(mailRequest);

            // On met à jour la request, pour garder une trace de l'envoi (objet, corps, pièces jointes)
            mailRequest.setSubject(mailDto.getSubject());
            mailRequest.setBody(mailDto.getBody());
            mailRequest.setAttachments(mailDto.getAttachments());

            // Envoyer le mail
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
        if (mailRequest.getType() == MailRequestType.INSCRIPTION) {
            return mailInscriptionService.createMail(mailRequest.getBusinessId());
        } else if (mailRequest.getType() == MailRequestType.ADHESION) {
            return mailAdhesionService.createMail(mailRequest.getBusinessId());
        }
        throw new IllegalStateException("Type de demande de mail non géré : " + mailRequest.getType());
    }

    private MimeMessage createMimeMessage(MailDto mailDto) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(mailDto.getRecipientEmail());
        helper.setSubject(mailDto.getSubject());
        helper.setText(mailDto.getBody(), true); // true = HTML
        
        // Ajout des éventuelles pièces jointes
        if (mailDto.getAttachments() != null) {
            for (MailAttachmentDto attachment : mailDto.getAttachments()) {
                FileSystemResource file = new FileSystemResource(new File(attachment.getLocation()));
                helper.addAttachment(attachment.getName(), file);
            }
        }
        
        return message;
    }
}
