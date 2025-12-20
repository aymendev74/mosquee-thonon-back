package org.mosqueethonon.service.impl.mail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.dto.mail.MailDto;
import org.mosqueethonon.entity.inscription.InfoMailInscriptionEntity;
import org.mosqueethonon.repository.InfoMailInscriptionRepository;
import org.mosqueethonon.service.mail.MailService;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.springframework.stereotype.Service;

@Service(MailInscriptionServiceImpl.MAIL_INSCRIPTION_SERVICE)
@Slf4j
@AllArgsConstructor
public class MailInscriptionServiceImpl implements MailService {

    public static final String MAIL_INSCRIPTION_SERVICE = "MAIL_INSCRIPTION_SERVICE";

    private InfoMailInscriptionRepository infoMailInscriptionRepository;

    private TraductionService traductionService;

    @Override
    public MailDto createMail(Long idInscription) {
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

        return MailDto.builder().subject(subject).body(body).recipientEmail(infoMail.getEmail()).build();
    }

}
