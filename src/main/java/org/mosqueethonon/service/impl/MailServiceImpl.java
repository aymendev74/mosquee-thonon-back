package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.enums.TypeMailEnum;
import org.mosqueethonon.service.MailService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.dto.MailObjectDto;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MailServiceImpl implements MailService {

    private JavaMailSender emailSender;

    @Transactional
    @Override
    public void sendEmailConfirmation(MailObjectDto mailObject, TypeMailEnum typeMail, String noInscription) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailObject.getEmail());
        message.setSubject(getEmailSubject(typeMail));
        message.setText(getEmailBody(mailObject, typeMail, noInscription));
        emailSender.send(message);
    }

    private String getEmailSubject(TypeMailEnum typeMail) {
        String mailSubject = null;
        switch (typeMail) {
            case ADHESION:
                mailSubject = "Votre demande d'adhésion à l'AMC";
                break;
            case COURS:
                mailSubject = "Votre demande inscription aux cours à l'AMC";
                break;
            default:
                throw new IllegalArgumentException("La valeur n'est pas géré ici ! typeMail = " + typeMail);
        }
        return mailSubject;
    }

    private String getEmailBody(MailObjectDto mailObject, TypeMailEnum typeMail, String noInscription) {
        StringBuilder sbMailBody = new StringBuilder("Assalam aleykoum ").append(mailObject.getPrenom())
                .append(" ").append(mailObject.getNom()).append(", \n\n");
        switch (typeMail) {
            case ADHESION:
                sbMailBody.append("Nous vous remercions pour votre demande d'adhésion, vous serez recontacté très rapidement pour la finaliser.");
                break;
            case COURS:
                sbMailBody.append("Nous vous remercions pour votre demande d'inscription aux cours, vous serez recontacté très rapidement pour la finaliser.");
                break;
            default:
                throw new IllegalArgumentException("La valeur n'est pas géré ici ! typeMail = " + typeMail);
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
