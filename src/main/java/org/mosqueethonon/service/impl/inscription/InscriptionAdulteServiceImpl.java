package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.enums.TypeInscriptionEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.inscription.InscriptionAdulteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class InscriptionAdulteServiceImpl implements InscriptionAdulteService {

    private Environment environment;

    private InscriptionAdulteRepository inscriptionAdulteRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionAdulteMapper inscriptionAdulteMapper;

    private TarifCalculService tarifCalculService;

    private ParamService paramService;

    private MailingConfirmationRepository mailingConfirmationRepository;

    @Override
    @Transactional
    public InscriptionAdulteDto createInscription(InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();

        // mapping vers l'entité
        InscriptionAdulteEntity entity = new InscriptionAdulteEntity();
        this.inscriptionAdulteMapper.mapDtoToEntity(inscription, entity);
        entity.setDateInscription(LocalDateTime.now());
        Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
        entity.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());
        entity.setStatut(StatutInscription.PROVISOIRE);

        // calcul du tarif
        this.calculTarif(entity, LocalDate.now(), inscription.getStatutProfessionnel());

        // On sauvegarde
        entity = this.inscriptionAdulteRepository.save(entity);

        // Envoi du mail si besoins (PROD et DEV uniquement, pas en STA, trop dangereux)
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return this.inscriptionAdulteMapper.fromEntityToDto(entity);
    }

    @Override
    public InscriptionAdulteDto findInscriptionById(Long id) {
        InscriptionAdulteEntity inscriptionAdulteEntity = this.inscriptionAdulteRepository.findById(id).orElse(null);
        if (inscriptionAdulteEntity != null) {
            return this.inscriptionAdulteMapper.fromEntityToDto(inscriptionAdulteEntity);
        }
        return null;
    }

    @Override
    @Transactional
    public InscriptionAdulteDto updateInscription(Long id, InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria) {
        InscriptionAdulteEntity entity = this.inscriptionAdulteRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new IllegalArgumentException("Inscription not found ! idinsc = " + id);
        }
        this.inscriptionAdulteMapper.mapDtoToEntity(inscription, entity);
        this.calculTarif(entity, null, inscription.getStatutProfessionnel());
        entity = this.inscriptionAdulteRepository.save(entity);
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return this.inscriptionAdulteMapper.fromEntityToDto(entity);
    }

    private void calculTarif(InscriptionAdulteEntity inscription, LocalDate atDate, StatutProfessionnelEnum statutPro) {
        LocalDate datRefCalcul = inscription.getDateInscription() != null ? inscription.getDateInscription().toLocalDate() : atDate;
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(inscription.getId(), datRefCalcul, statutPro);
        inscription.getResponsableLegal().setIdTarif(tarif.getIdTari());
        inscription.getEleves().forEach(e -> e.setIdTarif(tarif.getIdTari()));
        inscription.setMontantTotal(tarif.getTarif());
    }

    private void sendEmailIfRequired(Long idInscription, Boolean sendEmail) {
        if (Boolean.TRUE.equals(sendEmail)) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idInscription(idInscription)
                    .statut(MailingConfirmationStatut.PENDING).build());
        }
    }

    @Override
    public Integer findNbInscriptionsByPeriode(Long idPeriode) {
        return this.inscriptionRepository.getNbElevesInscritsByIdPeriode(idPeriode, TypeInscriptionEnum.ADULTE.name());
    }

    @Override
    public boolean isInscriptionOutsidePeriode(Long idPeriode, PeriodeDto periode) {
        Integer nbInscriptionOutside = this.inscriptionRepository.getNbInscriptionOutsideRange(idPeriode,
                periode.getDateDebut(), periode.getDateFin(), TypeInscriptionEnum.ADULTE.name());
        return nbInscriptionOutside != null && nbInscriptionOutside > 0;
    }
}
