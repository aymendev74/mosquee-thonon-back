package org.mosqueethonon.scheduled;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.inscription.InfoMailInscriptionEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingStatut;
import org.mosqueethonon.repository.InfoMailInscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.adhesion.AdhesionService;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TraductionService;
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
public class MailInscriptionJob {

    private JavaMailSender emailSender;

    private AdhesionService adhesionService;

    private InfoMailInscriptionRepository infoMailInscriptionRepository;

    private MailingConfirmationRepository mailingConfirmationRepository;

    private ParamService paramService;

    private TraductionService traductionService;

    @Scheduled(fixedDelayString = "${scheduled.confirmation-mail}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void sendPendingEmailsConfirmation() {
        List<MailingConfirmationEntity> mailingConfirmationsToProcess = mailingConfirmationRepository.findByStatut(MailingStatut.PENDING);
        if(!CollectionUtils.isEmpty(mailingConfirmationsToProcess)) {
            log.info("Il y a {} mails de confirmation à envoyer", mailingConfirmationsToProcess.size());
            mailingConfirmationsToProcess.forEach(this::processMail);
        }
    }

    private void processMail(MailingConfirmationEntity mailingConfirmationEntity) {
        if(mailingConfirmationEntity.getIdInscription() != null || mailingConfirmationEntity.getIdAdhesion() != null) {
            MimeMessage mail = null;
            try {
                if(mailingConfirmationEntity.getIdInscription() != null) {
                    mail = this.createMailInscription(mailingConfirmationEntity.getIdInscription());
                } else {
                    mail = this.createMailAdhesion(mailingConfirmationEntity.getIdAdhesion());
                }
            } catch (MessagingException e) {
                log.error("Erreur lors de la création du mail : ", e);
                mailingConfirmationEntity.setStatut(MailingStatut.ERROR);
                return;
            }
            if(paramService.isSendEmailEnabled() && mail != null) {
                log.info("Envoi du mail en cours");
                this.sendEmail(mailingConfirmationEntity, mail);
                log.info("Envoi du mail effectuée");
            } else {
                log.info("Envoi de mail ignoré");
                mailingConfirmationEntity.setStatut(MailingStatut.NOT_SENT);
            }
        } else {
            log.info("Pas normal, ni idInscription ni idAdhesion... idmaco = {} ", mailingConfirmationEntity.getId());
            mailingConfirmationEntity.setStatut(MailingStatut.ERROR);
        }
    }

    private MimeMessage createMailInscription(Long idInscription) throws MessagingException {
        log.info("Création du contenu du mail pour l'inscription idinsc = {}", idInscription);
        InfoMailInscriptionEntity infoMail = this.infoMailInscriptionRepository.findById(idInscription).orElse(null);
        if(infoMail == null) {
            log.error("Pas de données (infos mail) pour l'inscription idinsc = {}", idInscription);
            return null;
        }
        String bodyKey = "mail_inscription_" + infoMail.getStatut().name().toLowerCase();
        String subject = this.traductionService.findTraductionByCleAndValeur("mail_inscription", "subject").getFr();
        String bodyTemplate = this.traductionService.findTraductionByCleAndValeur(bodyKey, "body").getFr();
        String body = bodyTemplate
                .replace("@@{prenom}", infoMail.getPrenom())
                .replace("@@{nom}", infoMail.getNom())
                .replace("@@{noInscription}", infoMail.getNoInscription());
        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(infoMail.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true); // true = HTML
        return message;
    }

    private MimeMessage createMailAdhesion(Long idAdhesion) throws MessagingException {
        log.info("Création du contenu du mail pour l'adhésion idadhe = {}", idAdhesion);
        var adhesion = this.adhesionService.findAdhesionById(idAdhesion);
        if(adhesion == null) {
            log.error("Pas de données (adhésion) pour l'adhésion idadhe = {}", idAdhesion);
            return null;
        }
        String subject = this.traductionService.findTraductionByCleAndValeur("mail_adhesion", "subject").getFr();
        String bodyTemplate = this.traductionService.findTraductionByCleAndValeur("mail_adhesion", "body").getFr();
        String body = bodyTemplate
                .replace("@@{prenom}", adhesion.getPrenom())
                .replace("@@{nom}", adhesion.getNom());
        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(adhesion.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true); // true = HTML
        return message;
    }

    private void sendEmail(MailingConfirmationEntity mailingConfirmationEntity, MimeMessage mail) {
        try {
            this.emailSender.send(mail);
            mailingConfirmationEntity.setStatut(MailingStatut.DONE);
        } catch (Exception e) {
            String messageLog = null;
            if(mailingConfirmationEntity.getIdInscription() != null) {
                messageLog = "Problème lors de l'envoi du mail pour l'inscription idinsc = " + mailingConfirmationEntity.getIdInscription();
            } else {
                messageLog = "Problème lors de l'envoi du mail pour l'adhésion idadhe = " + mailingConfirmationEntity.getIdAdhesion();
            }
            mailingConfirmationEntity.setStatut(MailingStatut.ERROR);
            log.error(messageLog, e);
        }
    }
}
