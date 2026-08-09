package org.mosqueethonon.inscription.service.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.common.security.context.SecurityContext;
import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
import org.mosqueethonon.inscription.entity.ResponsableLegalEntity;
import org.mosqueethonon.referentiel.entity.PeriodeEntity;
import org.mosqueethonon.tarif.entity.TarifEntity;
import org.mosqueethonon.tarif.enums.ApplicationTarifEnum;
import org.mosqueethonon.document.enums.DocumentMetadataKeyEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;
import org.mosqueethonon.inscription.enums.NiveauScolaireEnum;
import org.mosqueethonon.inscription.enums.ResultatEnum;
import org.mosqueethonon.inscription.enums.TypeInscriptionEnum;
import org.mosqueethonon.document.service.AsyncDocumentService;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.document.entity.DocumentEntity;
import org.mosqueethonon.document.repository.DocumentRepository;
import org.mosqueethonon.inscription.repository.EleveRepository;
import org.mosqueethonon.inscription.repository.InscriptionEnfantRepository;
import org.mosqueethonon.referentiel.repository.NiveauRepository;
import org.mosqueethonon.referentiel.repository.PeriodeRepository;
import org.mosqueethonon.tarif.repository.TarifRepository;
import org.mosqueethonon.inscription.service.InscriptionEnfantService;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.mosqueethonon.paiement.service.PaiementService;
import org.mosqueethonon.param.service.ParamService;
import org.mosqueethonon.tarif.service.TarifCalculService;
import org.mosqueethonon.inscription.v1.dto.EleveDto;
import org.mosqueethonon.inscription.v1.dto.EleveReinscriptionDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantInfosDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantResultDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.inscription.v1.dto.ReinscriptionDto;
import org.mosqueethonon.inscription.v1.dto.ResponsableLegalDto;
import org.mosqueethonon.utilisateur.v1.dto.UserDto;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;
import org.mosqueethonon.tarif.v1.dto.TarifInscriptionEnfantDto;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;
import org.mosqueethonon.inscription.service.Incoherences;
import org.mosqueethonon.inscription.v1.mapper.EleveMapper;
import org.mosqueethonon.inscription.v1.mapper.InscriptionEnfantMapper;
import org.mosqueethonon.inscription.v1.mapper.ResponsableLegalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
@Slf4j
public class InscriptionEnfantServiceImpl extends CommonInscriptionService implements InscriptionEnfantService {

    private Clock clock;

    private InscriptionEnfantRepository inscriptionEnfantRepository;

    private InscriptionEnfantMapper inscriptionEnfantMapper;

    private TarifCalculService tarifCalculService;

    private ParamService paramService;

    private TarifRepository tarifRepository;

    private NiveauRepository niveauRepository;

    private SecurityContext securityContext;

    private ResponsableLegalMapper responsableLegalMapper;

    private EleveRepository eleveRepository;

    private EleveMapper eleveMapper;

    private PeriodeRepository periodeRepository;

    private AsyncDocumentService asyncDocumentService;

    private DocumentRepository documentRepository;

    private PaiementService paiementService;

    @Transactional
    @Override
    public InscriptionEnfantResultDto createInscription(InscriptionEnfantDto inscription) {
        if (!this.paramService.isInscriptionEnfantEnabled() || this.paramService.isReinscriptionPrioritaireEnabled()) {
            // En théorie cela ne devrait jamais arriver car si les inscriptions sont fermées, aucun tarif n'a pu être calculé pour l'utilisateur
            // Ou les réinscriptions prioritaires sont activées et donc on ne doit pas recevoir de nouvelles inscriptions
            RuntimeException e = new IllegalStateException("Les inscriptions sont actuellement fermées ! ");
            log.error("Les inscriptions sont actuellement fermées ! Et on a reçu une inscription, ceci est un cas anormal...", e);
            throw e;
        }
        // On lock la période en base pour gérer la liste d'attente lors des nouvelles inscriptions
        this.lockPeriodeActive(LocalDate.now(clock));
        inscription.normalize();
        InscriptionEnfantEntity entity = this.inscriptionEnfantMapper.fromDtoToEntity(inscription);

        ResponsableLegalDto responsableLegal = inscription.getResponsableLegal();
        UserAccountResult userAccountResult = this.manageUserAccount(responsableLegal.getEmail(), responsableLegal.getNom(), responsableLegal.getPrenom(), responsableLegal.getMobile());
        entity.setIdUtilisateur(userAccountResult.userId());

        TarifInscriptionEnfantDto tarifs = this.doCalculTarifInscription(entity);
        this.computeStatutNewInscription(entity, tarifs.isListeAttente());
        entity.setDateInscription(LocalDateTime.now(clock));
        entity.setNoInscription(this.generateNoInscription());
        entity = this.inscriptionEnfantRepository.save(entity);
        DocumentRequestEntity documentRequest = null;
        if(entity.getStatut() == StatutInscriptionEnum.PROVISOIRE || entity.getStatut() == StatutInscriptionEnum.VALIDEE) {
            documentRequest = this.asyncDocumentService.requestDocumentGeneration(DocumentRequestTypeEnum.INSCRIPTION_ENFANT, entity.getId());
        }
        this.createMailRequest(entity.getId(), documentRequest);

        return InscriptionEnfantResultDto.builder()
                .statut(entity.getStatut())
                .newlyCreatedAccount(userAccountResult.newlyCreated())
                .enabledAccount(userAccountResult.enabled())
                .build();
    }

    private void lockPeriodeActive(LocalDate dateInscription) {
        final LocalDate atDate = dateInscription != null ? dateInscription : LocalDate.now(clock);
        PeriodeEntity periode = this.periodeRepository.findByApplicationAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(ApplicationTarifEnum.COURS_ENFANT.name(), atDate, atDate)
                .orElseThrow(() -> new IllegalStateException("Aucune période active retrouvée - application : " + ApplicationTarifEnum.COURS_ENFANT.name() + " - date : " + atDate));
        this.periodeRepository.lockById(periode.getId());
    }

    @Override
    @Transactional
    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();
        InscriptionEnfantEntity entity = this.inscriptionEnfantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("L'inscription n'a pas été trouvée ! id = " + id));
        this.lockPeriodeActive(entity.getDateInscription().toLocalDate());
        StatutInscriptionEnum statutActuel = entity.getStatut();
        this.inscriptionEnfantMapper.updateInscriptionEntity(inscription, entity);

        this.doCalculTarifInscription(entity);
        this.checkStatutInscription(entity, statutActuel);
        entity = this.inscriptionEnfantRepository.save(entity);
        InscriptionEnfantDto resultInscription = this.inscriptionEnfantMapper.fromEntityToDto(entity);
        this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKeyEnum.ID_INSCRIPTION, String.valueOf(entity.getId()))
                .ifPresent(doc -> resultInscription.setIdDocument(doc.getId()));
        if (entity.getStatut() == StatutInscriptionEnum.PROVISOIRE || entity.getStatut() == StatutInscriptionEnum.VALIDEE) {
            var documentRequest = this.asyncDocumentService.requestDocumentGeneration(DocumentRequestTypeEnum.INSCRIPTION_ENFANT, entity.getId());
            if (Boolean.TRUE.equals(criteria.getSendMailConfirmation())) {
                this.createMailRequest(entity.getId(), documentRequest);
            }
        }
        return resultInscription;
    }

    private void computeStatutNewInscription(InscriptionEnfantEntity inscription, boolean isListeAttente) {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        if (isReinscriptionEnabled) {
            if (validateReinscription(inscription)) {
                // Si réinscription et que les élèves sont tous reconnus alors on valide directement l'inscription
                inscription.setStatut(StatutInscriptionEnum.VALIDEE);
            } else {
                // Sinon on la refuse
                inscription.setStatut(StatutInscriptionEnum.REFUSE);
            }
            return;
        }

        // Si pas réinscription alors soit on est en PROVISOIRE ou alors LISTE_ATTENTE
        if (isListeAttente) {
            inscription.setStatut(StatutInscriptionEnum.LISTE_ATTENTE);
            inscription.setNoPositionAttente(this.calculPositionAttente(inscription));
        } else {
            inscription.setStatut(StatutInscriptionEnum.PROVISOIRE);
        }
    }

    private void checkStatutInscription(InscriptionEnfantEntity inscription, StatutInscriptionEnum ancienStatut) {
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
                if (inscription.getStatut() == StatutInscriptionEnum.LISTE_ATTENTE) {
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
            Assert.state(tarif != null && tarif.getPeriode() != null,
                    "Le tarif et la période pour cette inscription n'ont pas pu être déterminés !");
            Assert.state(tarif.getPeriode().getIdPeriodePrecedente() != null,
                    "La période précédente n'existe pas sur la période actuelle ! idperi = " + tarif.getPeriode().getId());

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
        if (ancienEleve.getResultat() == ResultatEnum.NON_ACQUIS) {
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
        Assert.state(tarifs != null && tarifs.getIdTariBase() != null && tarifs.getIdTariEleve() != null,
                "Le tarif pour cette inscription n'a pas pu être déterminé !");
        inscription.setIdTarif(tarifs.getIdTariBase());
        inscription.getEleves().forEach(eleve -> eleve.setIdTarif(tarifs.getIdTariEleve()));
        inscription.setMontantTotal(this.calculMontantTotal(tarifs.getTarifBase(), tarifs.getTarifEleve(), nbEleves));
        return tarifs;
    }

    private BigDecimal calculMontantTotal(BigDecimal tarifBase, BigDecimal tarifEleve, Integer nbEleves) {
        return tarifBase.add(tarifEleve.multiply(BigDecimal.valueOf(nbEleves))).setScale(0, RoundingMode.HALF_UP);
    }

    private Integer calculPositionAttente(InscriptionEnfantEntity inscription) {
        LocalDate dateRefInscription = inscription.getDateInscription() != null ? inscription.getDateInscription().toLocalDate() : LocalDate.now(clock);
        Integer lastPosition = this.inscriptionEnfantRepository.getLastPositionAttente(dateRefInscription);
        return lastPosition != null ? ++lastPosition : 1;
    }

    @Override
    public InscriptionEnfantDto findInscriptionById(Long id) {
        InscriptionEnfantEntity inscriptionEnfantEntity = this.inscriptionEnfantRepository.findById(id).orElse(null);
        if (inscriptionEnfantEntity != null) {
            InscriptionEnfantDto dto = this.inscriptionEnfantMapper.fromEntityToDto(inscriptionEnfantEntity);
            this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKeyEnum.ID_INSCRIPTION, String.valueOf(id))
                    .ifPresent(doc -> dto.setIdDocument(doc.getId()));
            dto.setSituationPaiement(this.paiementService.getSituation(TypeCiblePaiementEnum.INSCRIPTION, id));
            return dto;
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
            LocalDateTime atDate = LocalDateTime.now(clock);
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
                        inscriptionEnAttente.setStatut(StatutInscriptionEnum.PROVISOIRE);
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
    public List<InscriptionEnfantParAnneeScolaireDto> findInscriptionsByUtilisateurConnecte() {
        String username = this.securityContext.getUser();
        Assert.state(username != null, "Aucun utilisateur connecté");

        UserDto utilisateur = this.userAccountManager.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + username));
        List<InscriptionEnfantEntity> inscriptions = this.inscriptionEnfantRepository.findByUtilisateurId(utilisateur.getId());

        return inscriptions.stream()
                .map(inscription -> {
                    TarifEntity tarif = this.tarifRepository.findById(inscription.getIdTarif()).orElse(null);
                    if (tarif == null || tarif.getPeriode() == null) {
                        return null;
                    }

                    List<EleveDto> eleveDtos = inscription.getEleves().stream()
                            .map(eleve -> this.eleveMapper.fromEntityToDto(eleve))
                            .collect(Collectors.toList());

                    ResponsableLegalDto responsableLegalDto = this.responsableLegalMapper.fromEntityToDto(inscription.getResponsableLegal());

                    // Récupérer l'idDocument associé à cette inscription
                    Long idDocument = this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKeyEnum.ID_INSCRIPTION, String.valueOf(inscription.getId()))
                            .map(DocumentEntity::getId)
                            .orElse(null);

                    return InscriptionEnfantParAnneeScolaireDto.builder()
                            .anneeDebut(tarif.getPeriode().getAnneeDebut())
                            .anneeFin(tarif.getPeriode().getAnneeFin())
                            .statut(inscription.getStatut())
                            .montantTotal(inscription.getMontantTotal())
                            .noInscription(inscription.getNoInscription())
                            .responsableLegal(responsableLegalDto)
                            .eleves(eleveDtos)
                            .idDocument(idDocument)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(InscriptionEnfantParAnneeScolaireDto::getAnneeDebut).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InscriptionEnfantDto reinscription(ReinscriptionDto reinscriptionDto) {
        Assert.isTrue(this.paramService.isInscriptionEnfantEnabled() && this.paramService.isReinscriptionPrioritaireEnabled(),
                "Les inscriptions/réinscriptions sont actuellement fermées !");
        Assert.notEmpty(reinscriptionDto.getEleves(), "Aucun élève sélectionné pour la réinscription");

        this.lockPeriodeActive(LocalDate.now(clock));

        String username = this.securityContext.getUser();
        Assert.state(username != null, "Aucun utilisateur connecté");

        UserDto utilisateur = this.userAccountManager.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + username));

        // Récupérer les élèves à réinscrire
        List<Long> elevesIds = reinscriptionDto.getEleves().stream().map(EleveReinscriptionDto::getId).toList();
        List<EleveEntity> elevesAReinscrire = this.eleveRepository.findAllById(elevesIds);
        Assert.isTrue(elevesAReinscrire.size() == elevesIds.size(),
                "Certains élèves n'ont pas été retrouvés");

        // Vérifier que les élèves appartiennent bien à l'utilisateur connecté
        for (EleveEntity eleve : elevesAReinscrire) {
            InscriptionEnfantEntity inscription = this.inscriptionEnfantRepository.findById(eleve.getIdInscription())
                    .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée pour l'élève : " + eleve.getId()));
            Assert.isTrue(inscription.getIdUtilisateur() != null &&
                            inscription.getIdUtilisateur().equals(utilisateur.getId()),
                    "L'élève " + eleve.getId() + " n'appartient pas à l'utilisateur connecté : " + utilisateur.getId());
        }

        // Créer un nouveau responsable légal à partir des données du DTO
        ResponsableLegalEntity responsableLegal = this.responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal());

        // Créer la nouvelle inscription
        InscriptionEnfantEntity nouvelleInscription = new InscriptionEnfantEntity();
        nouvelleInscription.setResponsableLegal(responsableLegal);
        nouvelleInscription.setIdUtilisateur(utilisateur.getId());
        nouvelleInscription.setDateInscription(LocalDateTime.now(clock));

        // Construire une map id -> niveau depuis le DTO
        Map<Long, NiveauScolaireEnum> niveauParEleve = reinscriptionDto.getEleves().stream()
                .collect(Collectors.toMap(EleveReinscriptionDto::getId, EleveReinscriptionDto::getNiveau));

        // Copier les élèves pour la nouvelle inscription
        List<EleveEntity> nouveauxEleves = elevesAReinscrire.stream()
                .map(e -> this.copierEleve(e, niveauParEleve.get(e.getId())))
                .toList();
        nouvelleInscription.setEleves(new ArrayList<>(nouveauxEleves));

        // Calculer le tarif et le statut
        TarifInscriptionEnfantDto tarifs = this.doCalculTarifInscription(nouvelleInscription);
        this.computeStatutNewInscription(nouvelleInscription, tarifs.isListeAttente());

        // Générer le numéro d'inscription
        nouvelleInscription.setNoInscription(this.generateNoInscription());

        // Marquer qu'il s'agit bien d'une réinscription passée par le processus dédié
        nouvelleInscription.setReinscription(Boolean.TRUE);

        nouvelleInscription = this.inscriptionEnfantRepository.save(nouvelleInscription);
        if (nouvelleInscription.getStatut() == StatutInscriptionEnum.PROVISOIRE || nouvelleInscription.getStatut() == StatutInscriptionEnum.VALIDEE) {
            var documentRequest = this.asyncDocumentService.requestDocumentGeneration(DocumentRequestTypeEnum.INSCRIPTION_ENFANT, nouvelleInscription.getId());
            this.createMailRequest(nouvelleInscription.getId(), documentRequest);
        }

        return this.inscriptionEnfantMapper.fromEntityToDto(nouvelleInscription);
    }

    private EleveEntity copierEleve(EleveEntity source, NiveauScolaireEnum niveau) {
        EleveEntity copie = new EleveEntity();
        copie.setNom(source.getNom());
        copie.setPrenom(source.getPrenom());
        copie.setDateNaissance(source.getDateNaissance());
        copie.setNiveau(niveau);
        copie.setSexe(source.getSexe());
        copie.setNiveauInterne(this.calculNiveauEleve(source));
        return copie;
    }

}
