package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.*;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.repository.*;
import org.mosqueethonon.service.InscriptionEnfantService;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.v1.dto.*;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.incoherences.Incoherences;
import org.mosqueethonon.v1.mapper.InscriptionEnfantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class InscriptionEnfantServiceImpl implements InscriptionEnfantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InscriptionEnfantServiceImpl.class);

    private InscriptionEnfantRepository inscriptionEnfantRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionEnfantMapper inscriptionEnfantMapper;
    private TarifCalculService tarifCalculService;

    private MailingConfirmationRepository mailingConfirmationRepository;
    private PeriodeRepository periodeRepository;

    private ParamService paramService;

    private ReinscriptionPrioritaireRepository reinscriptionPrioritaireRepository;

    @Transactional
    @Override
    public InscriptionEnfantDto saveInscription(InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        // Si pas en mode admin, on check si les inscriptions sont activées
        if(!Boolean.TRUE.equals(criteria.getIsAdmin())) {
            if(!this.paramService.isInscriptionEnabled()) {
                // En théorie cela ne devrait jamais arriver car si les inscriptions sont fermées, aucun tarif n'a pu être calculé pour l'utilisateur
                RuntimeException e = new IllegalStateException("Les inscriptions sont actuellement fermées ! ");
                LOGGER.error("Les inscriptions sont actuellement fermées ! Et on a reçu une inscription, ceci est un cas anormal...", e);
                throw e;
            }
        }
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();
        TarifInscriptionEnfantDto tarifs = this.doCalculTarifInscription(inscription, criteria.getIsAdmin());
        this.computeStatutInscription(inscription, tarifs.isListeAttente());
        InscriptionEnfantEntity entity = this.inscriptionEnfantMapper.fromDtoToEntity(inscription);
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
        entity = this.inscriptionEnfantRepository.save(entity);
        inscription = this.inscriptionEnfantMapper.fromEntityToDto(entity);
        if(Boolean.TRUE.equals(criteria.getSendMailConfirmation())) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idInscription(inscription.getId())
                    .statut(MailingConfirmationStatut.PENDING).build());
        }
        return inscription;
    }

    private void computeStatutInscription(InscriptionEnfantDto inscription, boolean isListeAttente) {
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

    private boolean isReinscriptionValide(InscriptionEnfantDto inscription) {
        for(EleveDto eleve : inscription.getEleves()) {
            ReinscriptionPrioritaireEntity reinscriptionPrio = this.reinscriptionPrioritaireRepository.findByNomAndPrenomPhonetique(eleve.getNom(), eleve.getPrenom());
            if(reinscriptionPrio == null) {
                return false;
            }
        }
        return true;
    }

    private TarifInscriptionEnfantDto doCalculTarifInscription(InscriptionEnfantDto inscription, Boolean isAdmin) {
        Integer nbEleves = inscription.getEleves().size();
        LocalDate atDate = inscription.getDateInscription() != null ?
                inscription.getDateInscription().toLocalDate() : LocalDate.now();
        InscriptionEnfantInfosDto inscriptionInfos = InscriptionEnfantInfosDto.builder().nbEleves(nbEleves)
                .adherent(inscription.getResponsableLegal().getAdherent())
                .isAdmin(isAdmin).atDate(atDate).build();
        TarifInscriptionEnfantDto tarifs = this.tarifCalculService.calculTarifInscriptionEnfant(inscriptionInfos);
        if(tarifs == null || tarifs.getIdTariBase() == null || tarifs.getIdTariEleve() == null) {
            throw new IllegalArgumentException("Le tarif pour cette inscription n'a pas pu être déterminé !");
        }
        inscription.getResponsableLegal().setIdTarif(tarifs.getIdTariBase());
        inscription.getEleves().forEach(eleve -> eleve.setIdTarif(tarifs.getIdTariEleve()));
        inscription.setMontantTotal(this.calculMontantTotal(tarifs.getTarifBase(), tarifs.getTarifEleve(), nbEleves));
        return tarifs;
    }

    private BigDecimal calculMontantTotal(BigDecimal tarifBase, BigDecimal tarifEleve, Integer nbEleves) {
        return tarifBase.add(tarifEleve.multiply(BigDecimal.valueOf(nbEleves))).setScale(0);
    }

    private Integer calculPositionAttente() {
        Integer lastPosition = this.inscriptionEnfantRepository.getLastPositionAttente(LocalDate.now());
        return lastPosition != null ? ++lastPosition : 1;
    }

    @Override
    public InscriptionEnfantDto findInscriptionById(Long id) {
        InscriptionEnfantEntity inscriptionEnfantEntity = this.inscriptionEnfantRepository.findById(id).orElse(null);
        if(inscriptionEnfantEntity !=null) {
            this.inscriptionEnfantMapper.fromEntityToDto(inscriptionEnfantEntity);
            return this.inscriptionEnfantMapper.fromEntityToDto(inscriptionEnfantEntity);
        }
        return null;
    }

    @Transactional
    @Override
    public Set<Long> validateInscriptions(Set<Long> ids) {
        List<InscriptionEnfantEntity> inscriptionsToUpdate = new ArrayList<>();
        for (Long id : ids) {
            InscriptionEnfantEntity inscription = this.inscriptionEnfantRepository.findById(id).orElse(null);
            if(inscription!=null) {
                inscription.setStatut(StatutInscription.VALIDEE);
                inscription.setNoPositionAttente(null);
                inscriptionsToUpdate.add(inscription);
            }
        }
        if(!CollectionUtils.isEmpty(inscriptionsToUpdate)) {
            inscriptionsToUpdate = this.inscriptionEnfantRepository.saveAll(inscriptionsToUpdate);
            return inscriptionsToUpdate.stream().map(InscriptionEnfantEntity::getId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Transactional
    @Override
    public Set<Long> deleteInscriptions(Set<Long> ids) {
        this.inscriptionEnfantRepository.deleteAllById(ids);
        // Maintenant que des inscriptions ont été supprimés, il faut aller voir si des inscriptions sont en liste d'attente et
        // les changer de statut => provisoire
        this.updateListeAttentePeriode(null);
        return ids;
    }

    @Override
    public void updateListeAttentePeriode(Long idPeriode) {
        Integer lastPositionAttente = null;
        if(idPeriode != null) {
            lastPositionAttente = inscriptionEnfantRepository.getLastPositionAttente(idPeriode);
        } else {
            lastPositionAttente = inscriptionEnfantRepository.getLastPositionAttente(LocalDate.now());
        }
        if(lastPositionAttente != null) {
            PeriodeEntity periode = null;
            if(idPeriode != null) {
                periode = this.periodeRepository.findById(idPeriode).orElse(null);
            } else {
                periode = this.periodeRepository.findPeriodeCoursAtDate(LocalDate.now());
            }
            Integer nbElevesInscrits = this.inscriptionEnfantRepository.getNbElevesInscritsByIdPeriode(periode.getId());
            if(nbElevesInscrits < periode.getNbMaxInscription()) {
                List<InscriptionEnfantEntity> inscriptionsEnAttente = this.inscriptionEnfantRepository.getInscriptionEnAttenteByPeriode(periode.getId());
                int nbPlacesDisponibles = periode.getNbMaxInscription() - nbElevesInscrits;
                    for(InscriptionEnfantEntity inscriptionEnAttente: inscriptionsEnAttente) {
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
                    this.inscriptionEnfantRepository.saveAll(inscriptionsEnAttente);
            }
        }
    }

    @Override
    public Integer findNbInscriptionsByPeriode(Long idPeriode) {
        return this.inscriptionEnfantRepository.getNbElevesInscritsByIdPeriode(idPeriode);
    }

    @Override
    public boolean isInscriptionOutsideRange(PeriodeDto periodeDto) {
        Integer nbInscriptionOutside = this.inscriptionEnfantRepository.getNbInscriptionOutsideRange(periodeDto.getId(),
                periodeDto.getDateDebut(), periodeDto.getDateFin());
        return nbInscriptionOutside != null && nbInscriptionOutside > 0;
    }

    @Override
    public String checkCoherence(InscriptionEnfantDto inscriptionEnfantDto) {
        inscriptionEnfantDto.normalize();
        return this.isAlreadyExistingEleves(inscriptionEnfantDto);
    }

    private String isAlreadyExistingEleves(InscriptionEnfantDto inscriptionEnfantDto) {
        if(!CollectionUtils.isEmpty(inscriptionEnfantDto.getEleves())) {
            LocalDateTime atDate = inscriptionEnfantDto.getDateInscription();
            if(atDate == null) {
                atDate = LocalDateTime.now();
            }
            for(EleveDto eleve : inscriptionEnfantDto.getEleves()) {
                if(eleve.getPrenom() != null && eleve.getNom() != null) {
                    List<InscriptionEnfantEntity> matchedInscriptions = this.inscriptionEnfantRepository.findInscriptionsWithEleve(eleve.getPrenom(),
                            eleve.getNom(), atDate, inscriptionEnfantDto.getId());
                    if(!CollectionUtils.isEmpty(matchedInscriptions)) {
                        return Incoherences.ELEVE_ALREADY_EXISTS;
                    }
                }
            }
        }
        return Incoherences.NO_INCOHERENCE;
    }
}