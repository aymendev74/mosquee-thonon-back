package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.enums.TypeMailEnum;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.MailObjectDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private JavaMailSender emailSender;

    private AdhesionService adhesionService;

    private InscriptionService inscriptionService;

    private MailingConfirmationRepository mailingConfirmationRepository;

    @Scheduled(fixedDelayString = "${scheduled.confirmation-mail}", timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void sendEmailConfirmation() {
        List<MailingConfirmationEntity> mailingConfirmationsToProcess = mailingConfirmationRepository.findByStatut(MailingConfirmationStatut.PENDING);
        if(!CollectionUtils.isEmpty(mailingConfirmationsToProcess)) {
            LOGGER.info("Il y a " + mailingConfirmationsToProcess.size() + " mails de confirmation à envoyer");
            mailingConfirmationsToProcess.stream().forEach(this::processMail);
        }
    }

    private void processMail(MailingConfirmationEntity mailingConfirmationEntity) {
        if(mailingConfirmationEntity.getIdInscription() != null || mailingConfirmationEntity.getIdAdhesion() != null) {
            SimpleMailMessage mail = null;
            if(mailingConfirmationEntity.getIdInscription() != null) {
                mail = this.createMailInscription(mailingConfirmationEntity.getIdInscription());
            } else {
                mail = this.createMailAdhesion(mailingConfirmationEntity.getIdAdhesion());
            }
            this.sendEmail(mailingConfirmationEntity, mail);
        } else {
            LOGGER.info("Pas normal, ni idInscription ni idAdhesion... idmaco = " + mailingConfirmationEntity.getId());
            mailingConfirmationEntity.setStatut(MailingConfirmationStatut.ERROR);
        }
    }

    private void sendEmail(MailingConfirmationEntity mailingConfirmationEntity, SimpleMailMessage mail) {
        try {
            this.emailSender.send(mail);
            mailingConfirmationEntity.setStatut(MailingConfirmationStatut.DONE);
        } catch (Exception e) {
            String messageLog = null;
            if(mailingConfirmationEntity.getIdInscription() != null) {
                messageLog = "Problème lors de l'envoi du mail pour l'inscription idinsc = " + mailingConfirmationEntity.getIdInscription();
            } else {
                messageLog = "Problème lors de l'envoi du mail pour l'adhésion idadhe = " + mailingConfirmationEntity.getIdAdhesion();
            }
            mailingConfirmationEntity.setStatut(MailingConfirmationStatut.ERROR);
            LOGGER.error(messageLog, e);
        }

    }

    private SimpleMailMessage createMailInscription(Long idInscription) {
        LOGGER.info("Envoi du mail pour l'inscription idinsc = " + idInscription);
        InscriptionDto inscription = this.inscriptionService.findInscriptionById(idInscription);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(inscription.getResponsableLegal().getEmail());
        message.setSubject(getEmailSubject(TypeMailEnum.COURS));
        message.setText(getEmailInscriptionBody(inscription.getResponsableLegal(), inscription.getNoInscription(), inscription.getStatut()));
        return message;
    }

    private SimpleMailMessage createMailAdhesion(Long idAdhesion) {
        LOGGER.info("Envoi du mail pour l'adhésion idadhe = " + idAdhesion);
        AdhesionDto adhesion = this.adhesionService.findAdhesionById(idAdhesion);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adhesion.getEmail());
        message.setSubject(getEmailSubject(TypeMailEnum.ADHESION));
        message.setText(getEmailAdhesionBody(adhesion));
        return message;
    }

    private String getEmailSubject(TypeMailEnum typeMail) {
        String mailSubject = null;
        switch (typeMail) {
            case ADHESION:
                mailSubject = "Votre demande d'adhésion à l'AMC";
                break;
            case COURS:
                mailSubject = "Votre demande d'inscription aux cours à l'AMC";
                break;
            default:
                throw new IllegalArgumentException("La valeur n'est pas gérée ici ! typeMail = " + typeMail);
        }
        return mailSubject;
    }

    private String getEmailInscriptionBody(MailObjectDto mailObject, String noInscription, StatutInscription statutInscription) {
        StringBuilder sbMailBody = new StringBuilder("Assalam aleykoum ").append(mailObject.getPrenom())
                .append(" ").append(mailObject.getNom()).append(", \n\n");

        switch(statutInscription) {
            case PROVISOIRE:
                sbMailBody.append("Nous vous remercions pour votre demande d'inscription aux cours.\n");
                sbMailBody.append("Votre inscription a été prise en compte, vous serez contacté rapidement par l'AMC pour la finaliser.");
                break;

            case VALIDEE:
                sbMailBody.append("Nous vous remercions pour votre demande d'inscription aux cours.\n");
                sbMailBody.append("Votre inscription a été validée.");
                break;

            case REFUSE:
                sbMailBody.append("Votre inscription a été refusée car actuellement, seules les réinscriptions sont autorisées.\n");
                sbMailBody.append("Si vous pensez qu'il s'agit d'une erreur, vous pouvez contacter directement l'AMC en répondant à cet e-mail.");
                break;

            case LISTE_ATTENTE:
                sbMailBody.append("Nous vous remercions pour votre demande d'inscription aux cours.\n");
                sbMailBody.append("Votre inscription a été prise en compte et elle a été placée sur liste d'attente.\n");
                sbMailBody.append("Vous serez contactés par l'AMC si des places se libèrent.");
                break;

            default:
                throw new IllegalArgumentException("La valeur n'est pas gérée ici ! statutInscription = " + statutInscription);
        }
        sbMailBody.append("\n\n");
        sbMailBody.append("Pour toute communication, voici la référence de votre inscription: ");
        sbMailBody.append(noInscription);
        sbMailBody.append("\n\n");
        sbMailBody.append("Cordialement,\n");
        sbMailBody.append("L'équipe de l'Association Musulmane du Chablais,\n");
        return sbMailBody.toString();
    }


    private String getEmailAdhesionBody(MailObjectDto mailObject) {
        StringBuilder sbMailBody = new StringBuilder("Assalam aleykoum ").append(mailObject.getPrenom())
                .append(" ").append(mailObject.getNom()).append(", \n\n");
        sbMailBody.append("Nous vous remercions pour votre demande d'adhésion, vous serez recontacté très rapidement pour la finaliser.");
        sbMailBody.append("\n\n");
        sbMailBody.append("Cordialement,\n");
        sbMailBody.append("L'équipe de l'Association Musulmane du Chablais,\n");
        return sbMailBody.toString();
    }
}
