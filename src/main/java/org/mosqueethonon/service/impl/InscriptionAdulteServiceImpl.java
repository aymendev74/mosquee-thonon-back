package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.ProfileNameProvider;
import org.mosqueethonon.entity.InscriptionAdulteEntity;
import org.mosqueethonon.entity.InscriptionEnfantEntity;
import org.mosqueethonon.entity.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.InscriptionAdulteService;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.v1.dto.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.InscriptionAdulteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

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

    private ProfileNameProvider profileNameProvider;

    @Override
    public InscriptionAdulteDto createInscription(InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();

        // mapping vers l'entité
        InscriptionAdulteEntity entity = this.inscriptionAdulteMapper.fromDtoToEntity(inscription);
        entity.setDateInscription(LocalDateTime.now());
        Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
        entity.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());
        entity.setAnneeScolaire(this.paramService.getAnneeScolaireEnCours());
        entity.setStatut(StatutInscription.PROVISOIRE);

        // calcul du tarif
        LocalDate atDate = inscription.getDateInscription() != null ?
                inscription.getDateInscription().toLocalDate() : LocalDate.now();
        this.calculTarif(entity, atDate);

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
    public InscriptionAdulteDto updateInscription(Long id, InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria) {
        InscriptionAdulteEntity entity = this.inscriptionAdulteRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new IllegalArgumentException("Inscription not found ! idinsc = " + id);
        }
        this.inscriptionAdulteMapper.updateInscriptionEntity(inscription, entity);
        this.calculTarif(entity, null);
        entity = this.inscriptionAdulteRepository.save(entity);
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return this.inscriptionAdulteMapper.fromEntityToDto(entity);
    }

    private void calculTarif(InscriptionAdulteEntity inscription, LocalDate atDate) {
        LocalDate datRefCalcul = inscription.getDateInscription() != null ? inscription.getDateInscription().toLocalDate() : atDate;
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(datRefCalcul);
        inscription.getResponsableLegal().setIdTarif(tarif.getIdTari());
        inscription.getEleves().forEach(e -> e.setIdTarif(tarif.getIdTari()));
        inscription.setMontantTotal(tarif.getTarif());
    }

    private void sendEmailIfRequired(Long idInscription, Boolean sendEmail) {
        if (profileNameProvider.isProdOrDev() && Boolean.TRUE.equals(sendEmail)) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idInscription(idInscription)
                    .statut(MailingConfirmationStatut.PENDING).build());
        }
    }
}
