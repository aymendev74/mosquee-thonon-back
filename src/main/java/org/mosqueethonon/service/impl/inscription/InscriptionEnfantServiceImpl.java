package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.*;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.*;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.EleveReinscriptionDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionDto;
import org.mosqueethonon.v1.dto.inscription.ResponsableLegalDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.incoherences.Incoherences;
import org.mosqueethonon.v1.mapper.inscription.InscriptionEnfantMapper;
import org.mosqueethonon.v1.mapper.inscription.ResponsableLegalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
@Slf4j
public class InscriptionEnfantServiceImpl implements InscriptionEnfantService {

    private InscriptionEnfantRepository inscriptionEnfantRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionEnfantMapper inscriptionEnfantMapper;

    private TarifCalculService tarifCalculService;

    private MailRequestRepository mailRequestRepository;

    private ParamService paramService;

    private TarifRepository tarifRepository;

    private NiveauRepository niveauRepository;

    private UtilisateurRepository utilisateurRepository;

    private SecurityContext securityContext;

    private ResponsableLegalMapper responsableLegalMapper;

    private EleveRepository eleveRepository;

    private org.mosqueethonon.v1.mapper.inscription.EleveMapper eleveMapper;

    @Transactional
    @Override
    public InscriptionEnfantDto createInscription(InscriptionEnfantDto inscription) {
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
        this.createMailRequest(entity.getId());
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
        if(Boolean.TRUE.equals(criteria.getSendMailConfirmation())) {
            this.createMailRequest(entity.getId());
        }
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

    private void createMailRequest(Long idInscription) {
        this.mailRequestRepository.save(MailRequestEntity.builder().businessId(idInscription)
                .type(MailRequestType.INSCRIPTION).statut(MailRequestStatut.PENDING).build());
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

    @Override
    public List<InscriptionEnfantParAnneeScolaireDto> findInscriptionsByUtilisateurConnecte() {
        String username = this.securityContext.getUser();
        Assert.state(username != null, "Aucun utilisateur connecté");
        
        UtilisateurEntity utilisateur = this.utilisateurRepository.findByUsername(username)
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

                    return InscriptionEnfantParAnneeScolaireDto.builder()
                            .anneeDebut(tarif.getPeriode().getAnneeDebut())
                            .anneeFin(tarif.getPeriode().getAnneeFin())
                            .statut(inscription.getStatut())
                            .montantTotal(inscription.getMontantTotal())
                            .noInscription(inscription.getNoInscription())
                            .responsableLegal(responsableLegalDto)
                            .eleves(eleveDtos)
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

        String username = this.securityContext.getUser();
        Assert.state(username != null, "Aucun utilisateur connecté");
        
        UtilisateurEntity utilisateur = this.utilisateurRepository.findByUsername(username)
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
            Assert.isTrue(inscription.getUtilisateur() != null &&
                    inscription.getUtilisateur().getId().equals(utilisateur.getId()),
                    "L'élève " + eleve.getId() + " n'appartient pas à l'utilisateur connecté : " + utilisateur.getId());
        }

        // Créer un nouveau responsable légal à partir des données du DTO
        ResponsableLegalEntity responsableLegal = this.responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal());

        // Créer la nouvelle inscription
        InscriptionEnfantEntity nouvelleInscription = new InscriptionEnfantEntity();
        nouvelleInscription.setResponsableLegal(responsableLegal);
        nouvelleInscription.setUtilisateur(utilisateur);
        nouvelleInscription.setDateInscription(LocalDateTime.now());

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
        Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
        nouvelleInscription.setNoInscription(new StringBuilder("AMC").append("-").append(noInscription).toString());

        nouvelleInscription = this.inscriptionEnfantRepository.save(nouvelleInscription);
        this.createMailRequest(nouvelleInscription.getId());

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
