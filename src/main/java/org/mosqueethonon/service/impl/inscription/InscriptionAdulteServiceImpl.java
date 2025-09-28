package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionMatiereEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.enums.TypeInscriptionEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.referentiel.MatiereService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class InscriptionAdulteServiceImpl implements InscriptionAdulteService {

    private InscriptionAdulteRepository inscriptionAdulteRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionAdulteMapper inscriptionAdulteMapper;

    private TarifCalculService tarifCalculService;

    private MailingConfirmationRepository mailingConfirmationRepository;

    private MatiereService matiereService;

    @Override
    @Transactional
    public InscriptionAdulteDto createInscription(InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();

        // mapping vers l'entité
        InscriptionAdulteEntity entity = new InscriptionAdulteEntity();
        this.inscriptionAdulteMapper.mapDtoToEntity(inscription, entity);
        entity.setMatieres(this.mapInscriptionMatieres(inscription));
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

    private List<InscriptionMatiereEntity> mapInscriptionMatieres(InscriptionAdulteDto inscription) {
        List<InscriptionMatiereEntity> inscriptionMatiereEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(inscription.getMatieres())) {
            for (MatiereEnum matiere : inscription.getMatieres()) {
                MatiereEntity matiereEntity = this.matiereService.findByCode(matiere)
                        .orElseThrow(() -> new ResourceNotFoundException("La matière " + matiere.name() + " n'a pas été trouvée"));
                InscriptionMatiereEntity inscriptionMatiereEntity = new InscriptionMatiereEntity();
                inscriptionMatiereEntity.setMatiere(matiereEntity);
                inscriptionMatiereEntities.add(inscriptionMatiereEntity);
            }
        }
        return inscriptionMatiereEntities;
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
        entity.getMatieres().clear();
        entity.getMatieres().addAll(this.mapInscriptionMatieres(inscription));
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
