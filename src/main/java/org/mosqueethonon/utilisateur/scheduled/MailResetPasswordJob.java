package org.mosqueethonon.utilisateur.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.utilisateur.entity.UserAccountActionEntity;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;
import org.mosqueethonon.mail.enums.MailRequestStatut;
import org.mosqueethonon.utilisateur.enums.UserAccountActionType;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.utilisateur.repository.UserAccountActionRepository;
import org.mosqueethonon.utilisateur.repository.UtilisateurRepository;
import org.mosqueethonon.param.service.ParamService;
import org.mosqueethonon.referentiel.service.TraductionService;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;
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
public class MailResetPasswordJob {

    private ParamService paramService;

    private JavaMailSender emailSender;

    private TraductionService traductionService;

    private UserAccountActionRepository userAccountActionRepository;

    private UtilisateurRepository utilisateurRepository;

    private ApplicationConfiguration applicationConfiguration;

    @Scheduled(fixedDelayString = "${scheduled.reset-password-mail}", timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void sendPendingEmailsResetPassword() {
        List<UserAccountActionEntity> resetPasswordRequests = userAccountActionRepository.findByStatutAndTypeOrderBySignatureDateCreationAsc(MailRequestStatut.PENDING, UserAccountActionType.RESET_PASSWORD);
        if (!CollectionUtils.isEmpty(resetPasswordRequests)) {
            log.info("Il y a {} mails de réinitialisation de mot de passe à envoyer", resetPasswordRequests.size());
            for (UserAccountActionEntity resetPasswordRequest : resetPasswordRequests) {
                MailRequestStatut statut;
                try {
                    statut = this.processMail(resetPasswordRequest);
                } catch (Exception e) {
                    log.error("Problème lors de l'envoi du mail de réinitialisation pour l'utilisateur {}", resetPasswordRequest.getUsername(), e);
                    statut = MailRequestStatut.ERROR;
                }
                resetPasswordRequest.setStatut(statut);
                userAccountActionRepository.save(resetPasswordRequest);
            }
        }
    }

    private MailRequestStatut processMail(UserAccountActionEntity accountAction) throws MessagingException {
        boolean isSendEmailDisabled = !this.paramService.isSendEmailEnabled();
        if (isSendEmailDisabled) {
            return MailRequestStatut.IGNORED;
        }

        UtilisateurEntity utilisateur = this.utilisateurRepository.findByUsername(accountAction.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé - username + " + accountAction.getUsername()));
        String subject = this.traductionService.findTraductionByCleAndValeur("mail_reset_password", "subject").getFr();
        TraductionDto bodyTemplate = this.traductionService.findTraductionByCleAndValeur("mail_reset_password", "body");

        String resetUrl = new StringBuilder(this.applicationConfiguration.getResetPasswordUri())
                .append("?")
                .append("token=")
                .append(accountAction.getToken())
                .toString();
        String body = bodyTemplate.getFr().replace("@@{username}", accountAction.getUsername())
                .replace("@@{resetUrl}", resetUrl);

        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(utilisateur.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);

        emailSender.send(message);

        return MailRequestStatut.SENT;
    }

}
