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
        LOGGER.info("Exécution du job d'envoi des emails de confirmation");
        List<MailingConfirmationEntity> mailingConfirmationsToProcess = mailingConfirmationRepository.findByStatut(MailingConfirmationStatut.PENDING);
        if(!CollectionUtils.isEmpty(mailingConfirmationsToProcess)) {
            LOGGER.info("Il y a " + mailingConfirmationsToProcess.size() + " mails de confirmation à envoyer");
            mailingConfirmationsToProcess.stream().forEach(this::processMailConfirmation);
        } else {
            LOGGER.info("Il n'y a aucun mail de confirmation à envoyer");
        }
    }

    private void processMailConfirmation(MailingConfirmationEntity mailingConfirmationEntity) {
        if(mailingConfirmationEntity.getIdInscription() != null || mailingConfirmationEntity.getIdAdhesion() != null) {
            if(mailingConfirmationEntity.getIdInscription() != null) {
                LOGGER.info("Envoi du mail pour l'inscription idinsc = " + mailingConfirmationEntity.getIdInscription());
                InscriptionDto inscription = this.inscriptionService.findInscriptionById(mailingConfirmationEntity.getIdInscription());
                boolean isRefus = inscription.getStatut() == StatutInscription.REFUSE;
                this.sendEmailConfirmation(inscription.getResponsableLegal(), TypeMailEnum.COURS, inscription.getNoInscription(),
                        isRefus);
            } else {
                LOGGER.info("Envoi du mail pour l'adhésion idadhe = " + mailingConfirmationEntity.getIdAdhesion());
                AdhesionDto adhesion = this.adhesionService.findAdhesionById(mailingConfirmationEntity.getIdAdhesion());
                this.sendEmailConfirmation(adhesion, TypeMailEnum.ADHESION, null, false);
            }
             mailingConfirmationEntity.setStatut(MailingConfirmationStatut.DONE);
        } else {
            LOGGER.info("Pas normal, ni idInscription ni idAdhesion... idmaco = " + mailingConfirmationEntity.getId());
            mailingConfirmationEntity.setStatut(MailingConfirmationStatut.ERROR);
        }
    }

    private void sendEmailConfirmation(MailObjectDto mailObject, TypeMailEnum typeMail, String noInscription, boolean refus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailObject.getEmail());
        message.setSubject(getEmailSubject(typeMail));
        message.setText(getEmailBody(mailObject, typeMail, noInscription, refus));
        emailSender.send(message);
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

    private String getEmailBody(MailObjectDto mailObject, TypeMailEnum typeMail, String noInscription, boolean refus) {
        StringBuilder sbMailBody = new StringBuilder("Assalam aleykoum ").append(mailObject.getPrenom())
                .append(" ").append(mailObject.getNom()).append(", \n\n");
        switch (typeMail) {
            case ADHESION:
                sbMailBody.append("Nous vous remercions pour votre demande d'adhésion, vous serez recontacté très rapidement pour la finaliser.");
                break;
            case COURS:
                if(refus) {
                    sbMailBody.append("Votre inscription a été refusée car actuellement, seules les réinscriptions sont autorisées.");
                    sbMailBody.append("Si vous pensez qu'il s'agit d'une erreur, vous pouvez contacter directement l'AMC en répondant à cet e-mail");
                } else {
                    sbMailBody.append("Nous vous remercions pour votre demande d'inscription aux cours.");
                }
                break;
            default:
                throw new IllegalArgumentException("La valeur n'est pas gérée ici ! typeMail = " + typeMail);
        }
        sbMailBody.append("\n\n");
        if(noInscription != null) {
            sbMailBody.append("Pour toute communication, voici la référence de votre inscription: ");
            sbMailBody.append(noInscription);
            sbMailBody.append("\n\n");
        }
        sbMailBody.append("Cordialement,\n");
        sbMailBody.append("L'équipe de l'Association Musulmane du Chablais,\n");
        return sbMailBody.toString();
    }
}
