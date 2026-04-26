package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionMatiereEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.enums.TypeInscriptionEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.*;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.inscription.InscriptionAdulteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
@Slf4j
public class InscriptionAdulteServiceImpl extends CommonInscriptionService implements InscriptionAdulteService {

    private InscriptionAdulteRepository inscriptionAdulteRepository;

    private InscriptionAdulteMapper inscriptionAdulteMapper;

    private TarifCalculService tarifCalculService;

    private MatiereService matiereService;

    private SecurityContext securityContext;

    private UtilisateurRepository utilisateurRepository;

    private TarifRepository tarifRepository;

    private ParamService paramService;

    private AsyncDocumentService asyncDocumentService;

    private DocumentRepository documentRepository;

    @Override
    @Transactional
    public InscriptionAdulteResultDto createInscription(InscriptionAdulteDto inscription) {
        // En théorie cela ne devrait jamais arriver car si les inscriptions sont fermées
        if(!paramService.isInscriptionAdulteEnabled()) {
            RuntimeException e = new IllegalStateException("Les inscriptions sont actuellement fermées ! ");
            log.error("Les inscriptions adultes sont actuellement fermées ! Et on a reçu une inscription, ceci est un cas anormal...", e);
            throw e;
        }

        // Normalisation des chaines de caractères saisies par l'utilisateur
        inscription.normalize();

        // mapping vers l'entité
        InscriptionAdulteEntity entity = new InscriptionAdulteEntity();
        this.inscriptionAdulteMapper.mapDtoToEntity(inscription, entity);
        entity.setMatieres(this.mapInscriptionMatieres(inscription));

        UserAccountResult userAccountResult = this.manageUserAccount(inscription.getEmail(), inscription.getNom(), inscription.getPrenom(), inscription.getMobile());
        entity.setIdUtilisateur(userAccountResult.userId());

        entity.setDateInscription(LocalDateTime.now());
        entity.setNoInscription(this.generateNoInscription());
        entity.setStatut(StatutInscription.PROVISOIRE);

        // calcul du tarif
        this.calculTarif(entity, LocalDate.now(), inscription.getStatutProfessionnel());

        // On sauvegarde
        entity = this.inscriptionAdulteRepository.save(entity);

        // Demande de génération asynchrone du document PDF
        // Le mail sera créé en NOT_READY et passera en PENDING une fois le document généré
        var documentRequest = this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.INSCRIPTION_ADULTE, entity.getId());
        this.createMailRequest(entity.getId(), documentRequest);

        return InscriptionAdulteResultDto.builder()
                .newlyCreatedAccount(userAccountResult.newlyCreated())
                .enabledAccount(userAccountResult.enabled())
                .build();
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
            InscriptionAdulteDto dto = this.inscriptionAdulteMapper.fromEntityToDto(inscriptionAdulteEntity);
            this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_INSCRIPTION, String.valueOf(id))
                    .ifPresent(doc -> dto.setIdDocument(doc.getId()));
            return dto;
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

        // Demande de régénération asynchrone du document PDF si nécessaire
        var documentRequest = this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.INSCRIPTION_ADULTE, entity.getId());

        if (Boolean.TRUE.equals(criteria.getSendMailConfirmation())) {
            this.createMailRequest(entity.getId(), documentRequest);
        }
        InscriptionAdulteDto dto = this.inscriptionAdulteMapper.fromEntityToDto(entity);
        this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_INSCRIPTION, String.valueOf(entity.getId()))
                .ifPresent(doc -> dto.setIdDocument(doc.getId()));
        return dto;
    }

    private void calculTarif(InscriptionAdulteEntity inscription, LocalDate atDate, StatutProfessionnelEnum statutPro) {
        LocalDate datRefCalcul = inscription.getDateInscription() != null ? inscription.getDateInscription().toLocalDate() : atDate;
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(inscription.getId(), datRefCalcul, statutPro);
        inscription.setIdTarif(tarif.getIdTari());
        inscription.getEleves().forEach(e -> e.setIdTarif(tarif.getIdTari()));
        inscription.setMontantTotal(tarif.getTarif());
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

    @Override
    @Transactional
    public InscriptionAdulteDto reinscription(ReinscriptionAdulteDto reinscriptionAdulteDto) {
        Assert.isTrue(this.paramService.isInscriptionAdulteEnabled(),
                "Les inscriptions adultes sont actuellement fermées !");

        InscriptionAdulteEntity entity = new InscriptionAdulteEntity();
        this.inscriptionAdulteMapper.mapReinscriptionDtoToEntity(reinscriptionAdulteDto, entity);
        entity.setMatieres(this.mapInscriptionMatieresFromList(reinscriptionAdulteDto.getMatieres()));

        UserAccountResult userAccountResult = this.manageUserAccount(reinscriptionAdulteDto.getEmail(),
                reinscriptionAdulteDto.getNom(), reinscriptionAdulteDto.getPrenom(), reinscriptionAdulteDto.getMobile());
        entity.setIdUtilisateur(userAccountResult.userId());

        entity.setDateInscription(LocalDateTime.now());
        entity.setNoInscription(this.generateNoInscription());
        entity.setStatut(StatutInscription.VALIDEE);

        this.calculTarif(entity, LocalDate.now(), reinscriptionAdulteDto.getStatutProfessionnel());

        entity = this.inscriptionAdulteRepository.save(entity);

        // Demande de génération asynchrone du document PDF
        var documentRequest = this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.INSCRIPTION_ADULTE, entity.getId());
        this.createMailRequest(entity.getId(), documentRequest);

        InscriptionAdulteDto dto = this.inscriptionAdulteMapper.fromEntityToDto(entity);
        return dto;
    }

    private List<InscriptionMatiereEntity> mapInscriptionMatieresFromList(List<MatiereEnum> matieres) {
        List<InscriptionMatiereEntity> inscriptionMatiereEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(matieres)) {
            for (MatiereEnum matiere : matieres) {
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
    public List<InscriptionAdulteParAnneeScolaireDto> findInscriptionsByUtilisateurConnecte() {
        String username = this.securityContext.getUser();
        Assert.state(username != null, "Aucun utilisateur connecté");

        UtilisateurEntity utilisateur = this.utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + username));

        // Récupération des inscriptions avec les élèves
        List<InscriptionAdulteEntity> inscriptions = this.inscriptionAdulteRepository.findByUtilisateurIdWithEleves(utilisateur.getId());
        
        // Chargement des matières dans une seconde requête pour éviter MultipleBagFetchException
        if (!inscriptions.isEmpty()) {
            this.inscriptionAdulteRepository.fetchMatieres(inscriptions);
        }

        return inscriptions.stream()
                .map(inscription -> {
                    TarifEntity tarif = this.tarifRepository.findById(inscription.getIdTarif())
                            .orElseThrow(() -> new ResourceNotFoundException("Tarif non trouvé : " + inscription.getIdTarif()));

                    EleveEntity eleve = inscription.getEleves().isEmpty() ? null : inscription.getEleves().get(0);

                    List<MatiereEnum> matieres = inscription.getMatieres().stream()
                            .map(m -> m.getMatiere().getCode())
                            .collect(Collectors.toList());

                    // Récupérer l'idDocument associé à cette inscription
                    Long idDocument = this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_INSCRIPTION, String.valueOf(inscription.getId()))
                            .map(doc -> doc.getId())
                            .orElse(null);

                    return InscriptionAdulteParAnneeScolaireDto.builder()
                            .anneeDebut(tarif.getPeriode().getAnneeDebut())
                            .anneeFin(tarif.getPeriode().getAnneeFin())
                            .statut(inscription.getStatut())
                            .montantTotal(inscription.getMontantTotal())
                            .noInscription(inscription.getNoInscription())
                            .nom(inscription.getResponsableLegal().getNom())
                            .prenom(inscription.getResponsableLegal().getPrenom())
                            .email(inscription.getResponsableLegal().getEmail())
                            .mobile(inscription.getResponsableLegal().getMobile())
                            .numeroEtRue(inscription.getResponsableLegal().getNumeroEtRue())
                            .codePostal(inscription.getResponsableLegal().getCodePostal())
                            .ville(inscription.getResponsableLegal().getVille())
                            .dateNaissance(eleve != null ? eleve.getDateNaissance() : null)
                            .sexe(eleve != null ? eleve.getSexe() : null)
                            .niveauInterne(eleve != null ? eleve.getNiveauInterne() : null)
                            .statutProfessionnel(inscription.getStatutProfessionnel())
                            .matieres(matieres)
                            .idDocument(idDocument)
                            .build();
                })
                .sorted(Comparator.comparing(InscriptionAdulteParAnneeScolaireDto::getAnneeDebut).reversed())
                .collect(Collectors.toList());
    }
}
