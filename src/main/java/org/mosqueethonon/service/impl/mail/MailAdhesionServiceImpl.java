package org.mosqueethonon.service.impl.mail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.dto.mail.MailAttachmentDto;
import org.mosqueethonon.dto.mail.MailDto;
import org.mosqueethonon.service.adhesion.AdhesionService;
import org.mosqueethonon.service.mail.MailService;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service(MailAdhesionServiceImpl.MAIL_ADHESION_SERVICE)
@Slf4j
@AllArgsConstructor
public class MailAdhesionServiceImpl implements MailService {

    public static final String MAIL_ADHESION_SERVICE = "MAIL_ADHESION_SERVICE";

    private TraductionService traductionService;

    private AdhesionService adhesionService;

    private ApplicationConfiguration applicationConfiguration;

    @Override
    public MailDto createMail(Long idAdhesion) {
        log.info("Création du contenu du mail pour l'adhésion idadhe = {}", idAdhesion);
        var adhesion = this.adhesionService.findAdhesionById(idAdhesion);
        if(adhesion == null) {
            log.error("Pas de données (adhésion) pour l'adhésion idadhe = {}", idAdhesion);
            return null;
        }
        String bodyKey = "mail_adhesion_" + adhesion.getStatut().name().toLowerCase();
        String subject = this.traductionService.findTraductionByCleAndValeur("mail_adhesion", "subject").getFr();
        String bodyTemplate = this.traductionService.findTraductionByCleAndValeur(bodyKey, "body").getFr();
        String body = bodyTemplate
                .replace("@@{prenom}", adhesion.getPrenom())
                .replace("@@{nom}", adhesion.getNom());
        var attachments = new ArrayList<MailAttachmentDto>();
        if(adhesion.getStatut() == StatutInscription.VALIDEE) {
            attachments.add(MailAttachmentDto.builder().name(applicationConfiguration.getRibAmc().getMailAttachmentFilename())
                    .location(applicationConfiguration.getRibAmc().getFileLocation()).build());
        }

        return MailDto.builder().body(body).subject(subject).recipientEmail(adhesion.getEmail())
                .attachments(attachments)
                .build();
    }

}
