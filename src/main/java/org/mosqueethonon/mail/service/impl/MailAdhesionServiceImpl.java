package org.mosqueethonon.mail.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.mail.dto.MailAttachmentDto;
import org.mosqueethonon.mail.dto.MailDto;
import org.mosqueethonon.adhesion.service.AdhesionService;
import org.mosqueethonon.mail.service.MailService;
import org.mosqueethonon.referentiel.service.TraductionService;
import org.mosqueethonon.inscription.enums.StatutInscription;
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

        return new MailDto().body(body).subject(subject).recipientEmail(adhesion.getEmail())
                .attachments(attachments);
    }

}
