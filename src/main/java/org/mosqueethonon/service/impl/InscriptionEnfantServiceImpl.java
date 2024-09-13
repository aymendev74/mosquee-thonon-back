package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.*;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.enums.TypeInscriptionEnum;
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
import java.util.List;

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
    public InscriptionEnfantDto createInscription(InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        if (!this.paramService.isInscriptionEnabled()) {
            // En théorie cela ne devrait jamais arriver car si les inscriptions sont fermées, aucun tarif n'a pu être calculé pour l'utilisateur
            RuntimeException e = new IllegalStateException("Les inscriptions sont actuellement fermées ! ");
            LOGGER.error("Les inscriptions sont actuellement fermées ! Et on a reçu une inscription, ceci est un cas anormal...", e);
            throw e;
        }
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();
        InscriptionEnfantEntity entity = this.inscriptionEnfantMapper.fromDtoToEntity(inscription);
        TarifInscriptionEnfantDto tarifs = this.doCalculTarifInscription(entity, criteria.getIsAdmin());
        this.computeStatutNewInscription(entity, tarifs.isListeAttente());
        entity.setDateInscription(LocalDateTime.now());
        Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
        entity.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());
        entity.setAnneeScolaire(this.paramService.getAnneeScolaireEnCours());
        entity = this.inscriptionEnfantRepository.save(entity);
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return this.inscriptionEnfantMapper.fromEntityToDto(entity);
    }

    @Override
    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();
        InscriptionEnfantEntity entity = this.inscriptionEnfantRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new IllegalArgumentException("L'inscription n'a pas été trouvée !");
        }
        StatutInscription statutActuel = entity.getStatut();
        this.inscriptionEnfantMapper.updateInscriptionEntity(inscription, entity);
        this.checkStatutInscription(entity, statutActuel);
        this.doCalculTarifInscription(entity, criteria.getIsAdmin());
        entity = this.inscriptionEnfantRepository.save(entity);
        inscription = this.inscriptionEnfantMapper.fromEntityToDto(entity);
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return inscription;
    }

    private void computeStatutNewInscription(InscriptionEnfantEntity inscription, boolean isListeAttente) {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        if (isReinscriptionEnabled) {
            if (isReinscriptionValide(inscription)) {
                // Si réinscription et que les élèves sont tous reconnus alors on valide directement l'inscription
                inscription.setStatut(StatutInscription.VALIDEE);
            } else {
                // Sinon on la refuse
                inscription.setStatut(StatutInscription.REFUSE);
            }
            return;
        }

        // Si pas réinscription alors soit on est en PROVISOIRE ou alors LISTE_ATTENTE
        if (isListeAttente) {
            inscription.setStatut(StatutInscription.LISTE_ATTENTE);
            inscription.setNoPositionAttente(this.calculPositionAttente());
        } else {
            inscription.setStatut(StatutInscription.PROVISOIRE);
        }
    }

    private void checkStatutInscription(InscriptionEnfantEntity inscription, StatutInscription ancienStatut) {
        // Si l'ancien statut est identique au nouveau (pas de changement), on ne fait rien
        if (inscription.getStatut() == ancienStatut) {
            return;
        }

        switch (ancienStatut) {
            case LISTE_ATTENTE:
                inscription.setNoPositionAttente(null);
                break;
            case PROVISOIRE:
            case VALIDEE:
            case REFUSE:
                if (inscription.getStatut() == StatutInscription.LISTE_ATTENTE) {
                    inscription.setNoPositionAttente(this.calculPositionAttente());
                }
                break;
            default:
                break;
        }
    }

    private boolean isReinscriptionValide(InscriptionEnfantEntity inscription) {
        for (EleveEntity eleve : inscription.getEleves()) {
            ReinscriptionPrioritaireEntity reinscriptionPrio = this.reinscriptionPrioritaireRepository.findByNomAndPrenomPhonetique(eleve.getNom(), eleve.getPrenom());
            if (reinscriptionPrio == null) {
                return false;
            }
        }
        return true;
    }

    private TarifInscriptionEnfantDto doCalculTarifInscription(InscriptionEnfantEntity inscription, Boolean isAdmin) {
        Integer nbEleves = inscription.getEleves().size();
        LocalDate atDate = inscription.getDateInscription() != null ?
                inscription.getDateInscription().toLocalDate() : LocalDate.now();
        InscriptionEnfantInfosDto inscriptionInfos = InscriptionEnfantInfosDto.builder().nbEleves(nbEleves)
                .adherent(inscription.getResponsableLegal().getAdherent())
                .isAdmin(isAdmin).atDate(atDate).build();
        TarifInscriptionEnfantDto tarifs = this.tarifCalculService.calculTarifInscriptionEnfant(inscriptionInfos);
        if (tarifs == null || tarifs.getIdTariBase() == null || tarifs.getIdTariEleve() == null) {
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
        if (inscriptionEnfantEntity != null) {
            return this.inscriptionEnfantMapper.fromEntityToDto(inscriptionEnfantEntity);
        }
        return null;
    }

    @Override
    public void updateListeAttentePeriode(Long idPeriode) {
        Integer lastPositionAttente = null;
        if (idPeriode != null) {
            lastPositionAttente = inscriptionEnfantRepository.getLastPositionAttente(idPeriode);
        } else {
            lastPositionAttente = inscriptionEnfantRepository.getLastPositionAttente(LocalDate.now());
        }
        if (lastPositionAttente != null) {
            PeriodeEntity periode = null;
            if (idPeriode != null) {
                periode = this.periodeRepository.findById(idPeriode).orElse(null);
            } else {
                periode = this.periodeRepository.findPeriodeCoursAtDate(LocalDate.now());
            }
            Integer nbElevesInscrits = this.inscriptionRepository.getNbElevesInscritsByIdPeriode(periode.getId(), TypeInscriptionEnum.ENFANT.name());
            if (nbElevesInscrits < periode.getNbMaxInscription()) {
                List<InscriptionEnfantEntity> inscriptionsEnAttente = this.inscriptionEnfantRepository.getInscriptionEnAttenteByPeriode(periode.getId());
                int nbPlacesDisponibles = periode.getNbMaxInscription() - nbElevesInscrits;
                for (InscriptionEnfantEntity inscriptionEnAttente : inscriptionsEnAttente) {
                    Integer nbEleveInscription = inscriptionEnAttente.getEleves().size();
                    if (nbEleveInscription <= nbPlacesDisponibles) {
                        // Le nombre d'élève à inscrire est inférieur ou égal au nombre de places restantes
                        inscriptionEnAttente.setStatut(StatutInscription.PROVISOIRE);
                        nbPlacesDisponibles = nbPlacesDisponibles - nbEleveInscription;
                    }
                    if (nbPlacesDisponibles == 0) {
                        break;
                    }
                }
                this.inscriptionEnfantRepository.saveAll(inscriptionsEnAttente);
            }
        }
    }

    @Override
    public Integer findNbInscriptionsByPeriode(Long idPeriode) {
        return this.inscriptionRepository.getNbElevesInscritsByIdPeriode(idPeriode, TypeInscriptionEnum.ENFANT.name());
    }

    @Override
    public boolean isInscriptionOutsideRange(PeriodeDto periodeDto) {
        Integer nbInscriptionOutside = this.inscriptionRepository.getNbInscriptionOutsideRange(periodeDto.getId(),
                periodeDto.getDateDebut(), periodeDto.getDateFin(), TypeInscriptionEnum.ENFANT.name());
        return nbInscriptionOutside != null && nbInscriptionOutside > 0;
    }

    @Override
    public String checkCoherence(Long idInscription, InscriptionEnfantDto inscriptionEnfantDto) {
        inscriptionEnfantDto.normalize();
        return this.isAlreadyExistingEleves(idInscription, inscriptionEnfantDto);
    }

    private String isAlreadyExistingEleves(Long idInscription, InscriptionEnfantDto inscriptionEnfantDto) {
        if (!CollectionUtils.isEmpty(inscriptionEnfantDto.getEleves())) {
            LocalDateTime atDate = LocalDateTime.now();
            if (idInscription != null) {
                InscriptionEnfantEntity inscription = this.inscriptionEnfantRepository.findById(idInscription).orElse(null);
                if (inscription != null) {
                    atDate = inscription.getDateInscription();
                }
            }
            for (EleveDto eleve : inscriptionEnfantDto.getEleves()) {
                if (eleve.getPrenom() != null && eleve.getNom() != null) {
                    List<InscriptionEnfantEntity> matchedInscriptions = this.inscriptionEnfantRepository.findInscriptionsWithEleve(eleve.getPrenom(),
                            eleve.getNom(), atDate, idInscription);
                    if (!CollectionUtils.isEmpty(matchedInscriptions)) {
                        return Incoherences.ELEVE_ALREADY_EXISTS;
                    }
                }
            }
        }
        return Incoherences.NO_INCOHERENCE;
    }

    private void sendEmailIfRequired(Long idInscription, Boolean sendEmail) {
        if (Boolean.TRUE.equals(sendEmail)) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idInscription(idInscription)
                    .statut(MailingConfirmationStatut.PENDING).build());
        }
    }
}
