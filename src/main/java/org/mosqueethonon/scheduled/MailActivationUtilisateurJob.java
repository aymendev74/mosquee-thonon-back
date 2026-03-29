package org.mosqueethonon.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.entity.mail.UserAccountActionEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.enums.UserAccountActionType;
import org.mosqueethonon.repository.UserAccountActionRepository;
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

    private UserAccountActionRepository userAccountActionRepository;

    private UtilisateurRepository utilisateurRepository;

    private ApplicationConfiguration applicationConfiguration;

    @Scheduled(fixedDelayString = "${scheduled.activation-utilisateur-mail}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void sendPendingEmailsActivation() {
        List<UserAccountActionEntity> mailingActivationsToProcess = userAccountActionRepository.findByStatutAndTypeOrderBySignatureDateCreationAsc(MailRequestStatut.PENDING, UserAccountActionType.ACTIVATION);
        if (!CollectionUtils.isEmpty(mailingActivationsToProcess)) {
            log.info("Il y a {} mails d'activation de compte à envoyer", mailingActivationsToProcess.size());
            for (UserAccountActionEntity accountAction : mailingActivationsToProcess) {
                MailRequestStatut statut;
                try {
                    statut = this.processMail(accountAction);
                } catch (Exception e) {
                    log.error("Problème lors de l'envoi du mail d'activation pour l'utilisateur {}", accountAction.getUsername(), e);
                    statut = MailRequestStatut.ERROR;
                }
                accountAction.setStatut(statut);
                userAccountActionRepository.save(accountAction);
            }
        }
    }

    private MailRequestStatut processMail(UserAccountActionEntity accountAction) throws MessagingException {
        // Si envoi des mails désactivé, on n'envoi pas le mail d'activation
        boolean isSendEmailDisabled = !this.paramService.isSendEmailEnabled();
        if (isSendEmailDisabled) {
            return MailRequestStatut.IGNORED;
        }

        UtilisateurEntity utilisateur = this.utilisateurRepository.findByUsername(accountAction.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé - username + " + accountAction.getUsername()));
        String subject = this.traductionService.findTraductionByCleAndValeur("mail_activation", "subject").getFr();
        TraductionDto bodyTemplate = this.traductionService.findTraductionByCleAndValeur("mail_activation", "body");

        // On remplace les placeholders par les valeurs réelles
        String urlActivation = new StringBuilder(this.applicationConfiguration.getActivationUtilisateurUri())
                .append("?")
                .append("token=")
                .append(accountAction.getToken())
                .toString();
        String username = utilisateur.getPrenom() != null ? utilisateur.getPrenom() : utilisateur.getUsername();
        String body = bodyTemplate.getFr().replace("@@{username}", username)
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
