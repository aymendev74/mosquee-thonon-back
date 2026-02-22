package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionMatiereEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.*;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
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
public class InscriptionAdulteServiceImpl implements InscriptionAdulteService {

    private InscriptionAdulteRepository inscriptionAdulteRepository;

    private InscriptionRepository inscriptionRepository;

    private InscriptionAdulteMapper inscriptionAdulteMapper;

    private TarifCalculService tarifCalculService;

    private MailRequestRepository mailRequestRepository;

    private MatiereService matiereService;

    private SecurityContext securityContext;

    private UtilisateurRepository utilisateurRepository;

    private TarifRepository tarifRepository;

    @Override
    @Transactional
    public InscriptionAdulteDto createInscription(InscriptionAdulteDto inscription) {
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

        // Envoi du mail de prise en compte
        this.createMailRequest(entity.getId());
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
        if(Boolean.TRUE.equals(criteria.getSendMailConfirmation())) {
            this.createMailRequest(entity.getId());
        }
        return this.inscriptionAdulteMapper.fromEntityToDto(entity);
    }

    private void calculTarif(InscriptionAdulteEntity inscription, LocalDate atDate, StatutProfessionnelEnum statutPro) {
        LocalDate datRefCalcul = inscription.getDateInscription() != null ? inscription.getDateInscription().toLocalDate() : atDate;
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(inscription.getId(), datRefCalcul, statutPro);
        inscription.setIdTarif(tarif.getIdTari());
        inscription.getEleves().forEach(e -> e.setIdTarif(tarif.getIdTari()));
        inscription.setMontantTotal(tarif.getTarif());
    }

    private void createMailRequest(Long idInscription) {
        this.mailRequestRepository.save(MailRequestEntity.builder().businessId(idInscription)
                .type(MailRequestType.INSCRIPTION).statut(MailRequestStatut.PENDING)
                .build());
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
                            .dateNaissance(eleve != null ? eleve.getDateNaissance() : null)
                            .sexe(eleve != null ? eleve.getSexe() : null)
                            .niveauInterne(eleve != null ? eleve.getNiveauInterne() : null)
                            .statutProfessionnel(inscription.getStatutProfessionnel())
                            .matieres(matieres)
                            .build();
                })
                .sorted(Comparator.comparing(InscriptionAdulteParAnneeScolaireDto::getAnneeDebut).reversed())
                .collect(Collectors.toList());
    }
}
