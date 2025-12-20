package org.mosqueethonon.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.entity.mail.MailingActivationUtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.MailingActivationUtilisateurRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class MailActivationUtilisateurJob {

    private ParamService paramService;

    private JavaMailSender emailSender;

    private TraductionService traductionService;

    private MailingActivationUtilisateurRepository mailingActivationUtilisateurRepository;

    private UtilisateurRepository utilisateurRepository;

    private ApplicationConfiguration applicationConfiguration;

    @Scheduled(fixedDelayString = "${scheduled.activation-utilisateur-mail}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void sendPendingEmailsActivation() {
        List<MailingActivationUtilisateurEntity> mailingActivationsToProcess = mailingActivationUtilisateurRepository.findByStatutOrderBySignatureDateCreationAsc(MailRequestStatut.PENDING);
        if (!CollectionUtils.isEmpty(mailingActivationsToProcess)) {
            log.info("Il y a {} mails d'activation de compte à envoyer", mailingActivationsToProcess.size());
            for (MailingActivationUtilisateurEntity mailingActivationUtilisateur : mailingActivationsToProcess) {
                MailRequestStatut statut;
                try {
                    statut = this.processMail(mailingActivationUtilisateur);
                } catch (MessagingException e) {
                    log.error("Problème lors de l'envoi du mail d'activation pour l'utilisateur {}", mailingActivationUtilisateur.getUsername(), e);
                    statut = MailRequestStatut.ERROR;
                }
                mailingActivationUtilisateur.setStatut(statut);
                mailingActivationUtilisateurRepository.save(mailingActivationUtilisateur);
            }
        }
    }

    private MailRequestStatut processMail(MailingActivationUtilisateurEntity mailingActivationUtilisateurEntity) throws MessagingException {
        // Si envoi des mails désactivé, on n'envoi pas le mail d'activation
        boolean isSendEmailDisabled = !this.paramService.isSendEmailEnabled();
        if (isSendEmailDisabled) {
            return MailRequestStatut.IGNORED;
        }

        UtilisateurEntity utilisateur = this.utilisateurRepository.findByUsername(mailingActivationUtilisateurEntity.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé - username + " + mailingActivationUtilisateurEntity.getUsername()));
        String subject = this.traductionService.findTraductionByCleAndValeur("mail_activation", "subject").getFr();
        TraductionDto bodyTemplate = this.traductionService.findTraductionByCleAndValeur("mail_activation", "body");

        // On remplace les placeholders par les valeurs réelles
        String urlActivation = new StringBuilder(this.applicationConfiguration.getActivationUtilisateurUri())
                .append("?")
                .append("token=")
                .append(mailingActivationUtilisateurEntity.getToken())
                .toString();
        String body = bodyTemplate.getFr().replace("@@{username}", mailingActivationUtilisateurEntity.getUsername())
                .replace("@@{activationUrl}", urlActivation);

        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(utilisateur.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true); // true = HTML

        emailSender.send(message);

        return MailRequestStatut.SENT;
    }

}
