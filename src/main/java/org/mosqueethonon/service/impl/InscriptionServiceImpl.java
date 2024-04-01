package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.entity.MailingConfirmationEntity;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.entity.ReinscriptionPrioritaireEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.ReinscriptionPrioritaireRepository;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.utils.DateUtils;
import org.mosqueethonon.v1.dto.*;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.InscriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private InscriptionRepository inscriptionRepository;
    private InscriptionMapper inscriptionMapper;
    private TarifCalculService tarifCalculService;

    private MailingConfirmationRepository mailingConfirmationRepository;
    private PeriodeRepository periodeRepository;

    private ParamService paramService;

    private ReinscriptionPrioritaireRepository reinscriptionPrioritaireRepository;

    @Transactional
    @Override
    public InscriptionDto saveInscription(InscriptionDto inscription) {
        TarifInscriptionDto tarifs = this.doCalculTarifInscription(inscription);
        this.computeStatutInscription(inscription, tarifs.isListeAttente());
        InscriptionEntity entity = this.inscriptionMapper.fromDtoToEntity(inscription);
        boolean sendMailConfirmation = inscription.getId() == null;
        if(entity.getDateInscription()==null) {
            entity.setDateInscription(LocalDate.now());
        }
        if(entity.getNoInscription() == null) {
            Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
            entity.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());
        }
        entity = this.inscriptionRepository.save(entity);
        inscription = this.inscriptionMapper.fromEntityToDto(entity);
        if(sendMailConfirmation) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idInscription(inscription.getId())
                    .statut(MailingConfirmationStatut.PENDING).build());
        }
        return inscription;
    }

    private void computeStatutInscription(InscriptionDto inscription, boolean isListeAttente) {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        if(isReinscriptionEnabled && inscription.getStatut() == null) {
            if(isReinscriptionValide(inscription)) {
                // Si réinscription et que les élèves sont tous reconnus alors on valide directement l'inscription
                inscription.setStatut(StatutInscription.VALIDEE);
            } else {
                // Sinon on la refuse
                inscription.setStatut(StatutInscription.REFUSE);
            }
            return;
        }

        // S'il ne s'agit pas de réinscription
        if(inscription.getStatut() == null) {
            if(isListeAttente) {
                inscription.setStatut(StatutInscription.LISTE_ATTENTE);
                inscription.setNoPositionAttente(this.calculPositionAttente());
            } else {
                inscription.setStatut(StatutInscription.PROVISOIRE);
            }
        } else {
            if(inscription.getStatut() == StatutInscription.LISTE_ATTENTE && inscription.getNoPositionAttente() == null) {
                inscription.setNoPositionAttente(this.calculPositionAttente());
            } else if (inscription.getStatut() != StatutInscription.LISTE_ATTENTE && inscription.getNoPositionAttente() != null){
                inscription.setNoPositionAttente(null);
            }
        }
    }

    private boolean isReinscriptionValide(InscriptionDto inscription) {
        for(EleveDto eleve : inscription.getEleves()) {
            ReinscriptionPrioritaireEntity reinscriptionPrio = this.reinscriptionPrioritaireRepository.findByNomIgnoreCaseAndPrenomIgnoreCaseAndDateNaissance
                    (eleve.getNom(), eleve.getPrenom(), DateUtils.fromStringToLocalDate(eleve.getDateNaissance()));
            if(reinscriptionPrio == null) {
                return false;
            }
        }
        return true;
    }

    private TarifInscriptionDto doCalculTarifInscription(InscriptionDto inscription) {
        InscriptionInfosDto inscriptionInfos = InscriptionInfosDto.builder().eleves(inscription.getEleves())
                .responsableLegal(inscription.getResponsableLegal()).build();
        TarifInscriptionDto tarifs = this.tarifCalculService.calculTarifInscription(inscriptionInfos);
        if(tarifs == null || tarifs.getIdTariBase() == null || tarifs.getIdTariEleve() == null) {
            throw new IllegalArgumentException("Le tarif pour cette inscription n'a pas pu être déterminé !");
        }
        inscription.getResponsableLegal().setIdTarif(tarifs.getIdTariBase());
        inscription.getEleves().forEach(eleve -> eleve.setIdTarif(tarifs.getIdTariEleve()));
        return tarifs;
    }

    private Integer calculPositionAttente() {
        Integer lastPosition = this.inscriptionRepository.getLastPositionAttente(LocalDate.now());
        return lastPosition != null ? ++lastPosition : 1;
    }

    @Override
    public InscriptionDto findInscriptionById(Long id) {
        InscriptionEntity inscriptionEntity = this.inscriptionRepository.findById(id).orElse(null);
        if(inscriptionEntity !=null) {
            return this.inscriptionMapper.fromEntityToDto(inscriptionEntity);
        }
        return null;
    }

    @Transactional
    @Override
    public Set<Long> validateInscriptions(Set<Long> ids) {
        List<InscriptionEntity> inscriptionsToUpdate = new ArrayList<>();
        for (Long id : ids) {
            InscriptionEntity inscription = this.inscriptionRepository.findById(id).orElse(null);
            if(inscription!=null) {
                inscription.setStatut(StatutInscription.VALIDEE);
                inscriptionsToUpdate.add(inscription);
            }
        }
        if(!CollectionUtils.isEmpty(inscriptionsToUpdate)) {
            inscriptionsToUpdate = this.inscriptionRepository.saveAll(inscriptionsToUpdate);
            return inscriptionsToUpdate.stream().map(InscriptionEntity::getId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Transactional
    @Override
    public Set<Long> deleteInscriptions(Set<Long> ids) {
        this.inscriptionRepository.deleteAllById(ids);
        // Maintenant que des inscriptions ont été supprimés, il faut aller voir si des inscriptions sont en liste d'attente et
        // les changer de statut => provisoire
        this.updateListeAttentePeriode(null);
        return ids;
    }

    @Override
    public void updateListeAttentePeriode(Long idPeriode) {
        Integer lastPositionAttente = null;
        if(idPeriode != null) {
            lastPositionAttente = inscriptionRepository.getLastPositionAttente(idPeriode);
        } else {
            lastPositionAttente = inscriptionRepository.getLastPositionAttente(LocalDate.now());
        }
        if(lastPositionAttente != null) {
            PeriodeEntity periode = null;
            if(idPeriode != null) {
                periode = this.periodeRepository.findById(idPeriode).orElse(null);
            } else {
                periode = this.periodeRepository.findPeriodeCoursAtDate(LocalDate.now());
            }
            Integer nbElevesInscrits = this.inscriptionRepository.getNbElevesInscritsByIdPeriode(periode.getId());
            if(nbElevesInscrits < periode.getNbMaxInscription()) {
                List<InscriptionEntity> inscriptionsEnAttente = this.inscriptionRepository.getInscriptionEnAttenteByPeriode(periode.getId());
                inscriptionsEnAttente = inscriptionsEnAttente.stream().sorted((i1, i2) -> i1.getNoPositionAttente().compareTo(i2.getNoPositionAttente()))
                        .collect(Collectors.toList());
                int nbPlacesDisponibles = periode.getNbMaxInscription() - nbElevesInscrits;
                    for(InscriptionEntity inscriptionEnAttente: inscriptionsEnAttente) {
                        Integer nbEleveInscription = inscriptionEnAttente.getEleves().size();
                        if(nbEleveInscription <= nbPlacesDisponibles) {
                            // Le nombre d'élève à inscrire est inférieur ou égal au nombre de places restantes
                            inscriptionEnAttente.setStatut(StatutInscription.PROVISOIRE);
                            nbPlacesDisponibles = nbPlacesDisponibles - nbEleveInscription;
                        }
                        if(nbPlacesDisponibles == 0) {
                            break;
                        }
                    }
                    this.inscriptionRepository.saveAll(inscriptionsEnAttente);
            }
        }
    }

    @Override
    public Integer findNbInscriptionsByPeriode(Long idPeriode) {
        return this.inscriptionRepository.getNbElevesInscritsByIdPeriode(idPeriode);
    }

    @Override
    public boolean isInscriptionOutsideRange(PeriodeDto periodeDto) {
        Integer nbInscriptionOutside = this.inscriptionRepository.getNbInscriptionOutsideRange(periodeDto.getId(),
                DateUtils.fromStringToLocalDate(periodeDto.getDateDebut()), DateUtils.fromStringToLocalDate(periodeDto.getDateFin()));
        return nbInscriptionOutside != null && nbInscriptionOutside > 0;
    }

}
