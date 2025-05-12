package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.ResultatEnum;
import org.mosqueethonon.enums.TypeInscriptionEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.*;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.incoherences.Incoherences;
import org.mosqueethonon.v1.mapper.inscription.InscriptionEnfantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
@Slf4j
public class InscriptionEnfantServiceImpl implements InscriptionEnfantService {

    private InscriptionEnfantRepository inscriptionEnfantRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionEnfantMapper inscriptionEnfantMapper;

    private TarifCalculService tarifCalculService;

    private MailingConfirmationRepository mailingConfirmationRepository;

    private ParamService paramService;

    private TarifRepository tarifRepository;

    private NiveauRepository niveauRepository;

    @Transactional
    @Override
    public InscriptionEnfantDto createInscription(InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        if (!this.paramService.isInscriptionEnfantEnabled()) {
            // En théorie cela ne devrait jamais arriver car si les inscriptions sont fermées, aucun tarif n'a pu être calculé pour l'utilisateur
            RuntimeException e = new IllegalStateException("Les inscriptions sont actuellement fermées ! ");
            log.error("Les inscriptions sont actuellement fermées ! Et on a reçu une inscription, ceci est un cas anormal...", e);
            throw e;
        }
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();
        InscriptionEnfantEntity entity = this.inscriptionEnfantMapper.fromDtoToEntity(inscription);
        TarifInscriptionEnfantDto tarifs = this.doCalculTarifInscription(entity);
        this.computeStatutNewInscription(entity, tarifs.isListeAttente());
        entity.setDateInscription(LocalDateTime.now());
        Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
        entity.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());
        entity = this.inscriptionEnfantRepository.save(entity);
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return this.inscriptionEnfantMapper.fromEntityToDto(entity);
    }

    @Override
    @Transactional
    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();
        InscriptionEnfantEntity entity = this.inscriptionEnfantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("L'inscription n'a pas été trouvée ! id = " + id));
        StatutInscription statutActuel = entity.getStatut();
        this.inscriptionEnfantMapper.updateInscriptionEntity(inscription, entity);

        this.doCalculTarifInscription(entity);
        this.checkStatutInscription(entity, statutActuel);
        entity = this.inscriptionEnfantRepository.save(entity);
        inscription = this.inscriptionEnfantMapper.fromEntityToDto(entity);
        this.sendEmailIfRequired(entity.getId(), criteria.getSendMailConfirmation());
        return inscription;
    }

    private void computeStatutNewInscription(InscriptionEnfantEntity inscription, boolean isListeAttente) {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        if (isReinscriptionEnabled) {
            if (validateReinscription(inscription)) {
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
            inscription.setNoPositionAttente(this.calculPositionAttente(inscription));
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
                    inscription.setNoPositionAttente(this.calculPositionAttente(inscription));
                }
                break;
            default:
                break;
        }
    }

    private boolean validateReinscription(InscriptionEnfantEntity inscription) {
        for (EleveEntity eleve : inscription.getEleves()) {
            TarifEntity tarif = this.tarifRepository.findById(eleve.getIdTarif()).orElse(null);
            if(tarif == null || tarif.getPeriode() == null) {
                throw new IllegalArgumentException("Le tarif et la période pour cette inscription n'ont pas pu être déterminés !");
            }
            if(tarif.getPeriode().getIdPeriodePrecedente() == null) {
                throw new IllegalArgumentException("La période précédente n'existe pas sur la période actuelle ! idperi = " + tarif.getPeriode().getId());
            }
            EleveEntity ancienEleve = this.inscriptionRepository.findFirstEleveByNomPrenomDateNaissanceIdPeriode(eleve.getNom(), eleve.getPrenom(),
                    eleve.getDateNaissance(), tarif.getPeriode().getIdPeriodePrecedente());
            if (ancienEleve == null) {
                return false;
            }
            // On calcule le nouveau niveau de l'élève pour cette année, basé sur son niveau et son résultat de l'année précédente
            eleve.setNiveauInterne(this.calculNiveauEleve(ancienEleve));
        }
        return true;
    }

    private NiveauInterneEnum calculNiveauEleve(EleveEntity ancienEleve) {
        if (ancienEleve.getNiveauInterne() == null || ancienEleve.getResultat() == null) {
            log.warn("Impossible de calculer le nouveau niveau car l'ancien niveau ou le résultat de l'élève n'existe pas ! idelev = {}", ancienEleve.getId());
            return null;
        }
        // Si année non validée alors l'élève reste dans le niveau de l'année précédente
        if(ancienEleve.getResultat() == ResultatEnum.NON_ACQUIS) {
            return ancienEleve.getNiveauInterne();
        }
        // Sinon il passe au niveau suivant
        return this.niveauRepository.findNiveauSuperieurByNiveau(ancienEleve.getNiveauInterne());
    }

    private TarifInscriptionEnfantDto doCalculTarifInscription(InscriptionEnfantEntity inscription) {
        Integer nbEleves = inscription.getEleves().size();
        InscriptionEnfantInfosDto inscriptionInfos = InscriptionEnfantInfosDto.builder().nbEleves(nbEleves)
                .adherent(inscription.getResponsableLegal().getAdherent()).build();
        TarifInscriptionEnfantDto tarifs = this.tarifCalculService.calculTarifInscriptionEnfant(inscription.getId(), inscriptionInfos);
        if (tarifs == null || tarifs.getIdTariBase() == null || tarifs.getIdTariEleve() == null) {
            throw new IllegalArgumentException("Le tarif pour cette inscription n'a pas pu être déterminé !");
        }
        inscription.getResponsableLegal().setIdTarif(tarifs.getIdTariBase());
        inscription.getEleves().forEach(eleve -> eleve.setIdTarif(tarifs.getIdTariEleve()));
        inscription.setMontantTotal(this.calculMontantTotal(tarifs.getTarifBase(), tarifs.getTarifEleve(), nbEleves));
        return tarifs;
    }

    private BigDecimal calculMontantTotal(BigDecimal tarifBase, BigDecimal tarifEleve, Integer nbEleves) {
        return tarifBase.add(tarifEleve.multiply(BigDecimal.valueOf(nbEleves))).setScale(0, RoundingMode.HALF_UP);
    }

    private Integer calculPositionAttente(InscriptionEnfantEntity inscription) {
        LocalDate dateRefInscription = inscription.getDateInscription() != null ? inscription.getDateInscription().toLocalDate() : LocalDate.now();
        Integer lastPosition = this.inscriptionEnfantRepository.getLastPositionAttente(dateRefInscription);
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
    public Integer findNbInscriptionsByPeriode(Long idPeriode) {
        return this.inscriptionRepository.getNbElevesInscritsByIdPeriode(idPeriode, TypeInscriptionEnum.ENFANT.name());
    }

    @Override
    public boolean isInscriptionOutsidePeriode(Long id, PeriodeDto periodeDto) {
        Integer nbInscriptionOutside = this.inscriptionRepository.getNbInscriptionOutsideRange(id,
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
                            eleve.getNom(), eleve.getDateNaissance(), atDate.toLocalDate(), idInscription);
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

    @Override
    public Integer getNbElevesInscritsByIdPeriode(Long idPeriode) {
        return this.inscriptionRepository.getNbElevesInscritsByIdPeriode(idPeriode, TypeInscriptionEnum.ENFANT.name());
    }

    @Override
    public void updateListeAttente(Long idPeriode, Integer nbMaxInscriptions) {
        Integer lastPositionAttente = this.inscriptionEnfantRepository.getLastPositionAttente(idPeriode);
        if (lastPositionAttente != null) {
            Integer nbElevesInscrits = this.inscriptionRepository.getNbElevesInscritsByIdPeriode(idPeriode, TypeInscriptionEnum.ENFANT.name());
            if (nbMaxInscriptions != null && nbElevesInscrits < nbMaxInscriptions) {
                List<InscriptionEnfantEntity> inscriptionsEnAttente = this.inscriptionEnfantRepository.getInscriptionEnAttenteByPeriode(idPeriode);
                int nbPlacesDisponibles = nbMaxInscriptions - nbElevesInscrits;
                for (InscriptionEnfantEntity inscriptionEnAttente : inscriptionsEnAttente) {
                    int nbEleveInscription = inscriptionEnAttente.getEleves().size();
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
}
