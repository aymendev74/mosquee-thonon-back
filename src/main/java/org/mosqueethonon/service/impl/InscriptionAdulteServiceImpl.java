package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import org.mosqueethonon.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.InscriptionAdulteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class InscriptionAdulteServiceImpl implements InscriptionAdulteService {

    private InscriptionAdulteRepository inscriptionAdulteRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionAdulteMapper inscriptionAdulteMapper;

    private TarifCalculService tarifCalculService;

    private ParamService paramService;

    private MailingConfirmationRepository mailingConfirmationRepository;

    @Override
    public InscriptionAdulteDto saveInscription(InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();

        // calcul du tarif
        LocalDate atDate = inscription.getDateInscription() != null ?
                inscription.getDateInscription().toLocalDate() : LocalDate.now();
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(atDate);
        inscription.setIdTarif(tarif.getIdTari());
        inscription.setMontantTotal(tarif.getTarif());

        // mapping vers l'entité
        InscriptionAdulteEntity entity = this.inscriptionAdulteMapper.fromDtoToEntity(inscription);
        if(entity.getDateInscription()==null) {
            entity.setDateInscription(LocalDateTime.now());
        }
        if(entity.getNoInscription() == null) {
            Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
            entity.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());
        }
        if(entity.getAnneeScolaire() == null) {
            entity.setAnneeScolaire(this.paramService.getAnneeScolaireEnCours());
        }
        if(entity.getStatut() == null) {
            entity.setStatut(StatutInscription.PROVISOIRE);
        }
        entity = this.inscriptionAdulteRepository.save(entity);
        inscription = this.inscriptionAdulteMapper.fromEntityToDto(entity);
        /*if(Boolean.TRUE.equals(criteria.getSendMailConfirmation())) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idInscription(inscription.getId())
                    .statut(MailingConfirmationStatut.PENDING).build());
        }*/
        return inscription;
    }
}
