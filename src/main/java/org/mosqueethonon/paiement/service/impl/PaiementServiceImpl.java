package org.mosqueethonon.paiement.service.impl;

import lombok.RequiredArgsConstructor;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.inscription.entity.InscriptionEntity;
import org.mosqueethonon.inscription.repository.InscriptionRepository;
import org.mosqueethonon.paiement.entity.PaiementEntity;
import org.mosqueethonon.paiement.enums.ModePaiementEnum;
import org.mosqueethonon.paiement.enums.StatutPaiementEnum;
import org.mosqueethonon.paiement.enums.StatutReglementEnum;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.mosqueethonon.paiement.exception.PaiementErreurEnum;
import org.mosqueethonon.paiement.exception.PaiementValidationException;
import org.mosqueethonon.paiement.repository.PaiementRepository;
import org.mosqueethonon.paiement.service.PaiementService;
import org.mosqueethonon.paiement.v1.dto.PaiementDto;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;
import org.mosqueethonon.paiement.v1.mapper.PaiementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;

    private final InscriptionRepository inscriptionRepository;

    private final PaiementMapper paiementMapper;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public SituationPaiementDto getSituation(TypeCiblePaiementEnum typeCible, Long idCible) {
        return this.buildSituation(typeCible, idCible, this.getMontantDu(typeCible, idCible));
    }

    @Override
    @Transactional
    public SituationPaiementDto creer(PaiementDto paiement) {
        TypeCiblePaiementEnum typeCible = paiement.getTypeCible();
        Long idCible = paiement.getIdCible();
        BigDecimal montantDu = this.getMontantDu(typeCible, idCible);

        this.validerDate(paiement.getDatePaiement());
        this.validerMode(paiement.getMode());
        this.validerMontant(paiement.getMontant(), montantDu, this.getMontantRegle(typeCible, idCible));

        PaiementEntity entity = this.paiementMapper.fromDtoToEntity(paiement);
        entity.setStatut(StatutPaiementEnum.VALIDE);
        this.paiementRepository.save(entity);

        return this.buildSituation(typeCible, idCible, montantDu);
    }

    @Override
    @Transactional
    public SituationPaiementDto modifier(Long id, PaiementDto paiement) {
        PaiementEntity entity = this.findPaiementById(id);
        this.assertModifiable(entity);

        // La cible d'un paiement n'est pas modifiable : un règlement ne se déplace pas d'une
        // inscription à une autre, il s'annule et se ressaisit.
        TypeCiblePaiementEnum typeCible = entity.getTypeCible();
        Long idCible = entity.getIdCible();
        BigDecimal montantDu = this.getMontantDu(typeCible, idCible);

        // Le paiement modifié est déjà compté dans le montant réglé : on l'en retire avant de
        // contrôler, sans quoi toute augmentation d'un règlement existant serait rejetée à tort.
        // Le retrait est légitime ici car assertModifiable garantit que le paiement est VALIDE.
        BigDecimal montantRegleHorsCourant = this.getMontantRegle(typeCible, idCible)
                .subtract(entity.getMontant());

        this.validerDate(paiement.getDatePaiement());
        this.validerMode(paiement.getMode());
        this.validerMontant(paiement.getMontant(), montantDu, montantRegleHorsCourant);

        entity.setMontant(paiement.getMontant());
        entity.setDatePaiement(paiement.getDatePaiement());
        entity.setMode(paiement.getMode());
        entity.setReference(paiement.getReference());
        entity.setCommentaire(paiement.getCommentaire());
        this.paiementRepository.save(entity);

        return this.buildSituation(typeCible, idCible, montantDu);
    }

    @Override
    @Transactional
    public SituationPaiementDto annuler(Long id) {
        PaiementEntity entity = this.findPaiementById(id);
        this.assertModifiable(entity);

        entity.setStatut(StatutPaiementEnum.ANNULE);
        this.paiementRepository.save(entity);

        return this.buildSituation(entity.getTypeCible(), entity.getIdCible(),
                this.getMontantDu(entity.getTypeCible(), entity.getIdCible()));
    }

    private SituationPaiementDto buildSituation(TypeCiblePaiementEnum typeCible, Long idCible, BigDecimal montantDu) {
        BigDecimal montantRegle = this.getMontantRegle(typeCible, idCible);
        List<PaiementEntity> paiements =
                this.paiementRepository.findByTypeCibleAndIdCibleOrderByDatePaiementAscIdAsc(typeCible, idCible);
        return SituationPaiementDto.builder()
                .typeCible(typeCible)
                .idCible(idCible)
                .montantTotal(montantDu)
                .montantRegle(montantRegle)
                .resteAPayer(montantDu.subtract(montantRegle))
                .statutReglement(StatutReglementEnum.of(montantDu, montantRegle))
                .paiements(this.paiementMapper.fromEntitiesToDtos(paiements))
                .build();
    }

    /**
     * Résout le montant dû par la cible. C'est le seul point à étendre le jour où les adhésions
     * deviendront réglables.
     */
    private BigDecimal getMontantDu(TypeCiblePaiementEnum typeCible, Long idCible) {
        if (typeCible == null || idCible == null) {
            throw new PaiementValidationException(PaiementErreurEnum.CIBLE_INTROUVABLE,
                    "Le type et l'identifiant de la cible du paiement sont obligatoires");
        }
        return switch (typeCible) {
            case INSCRIPTION -> {
                InscriptionEntity inscription = this.inscriptionRepository.findById(idCible)
                        .orElseThrow(() -> new PaiementValidationException(PaiementErreurEnum.CIBLE_INTROUVABLE,
                                "Aucune inscription trouvée pour l'identifiant " + idCible));
                yield inscription.getMontantTotal() != null ? inscription.getMontantTotal() : BigDecimal.ZERO;
            }
        };
    }

    private BigDecimal getMontantRegle(TypeCiblePaiementEnum typeCible, Long idCible) {
        BigDecimal montantRegle = this.paiementRepository.sumMontantValide(typeCible, idCible);
        return montantRegle != null ? montantRegle : BigDecimal.ZERO;
    }

    private PaiementEntity findPaiementById(Long id) {
        return this.paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun paiement trouvé pour l'identifiant " + id));
    }

    private void assertModifiable(PaiementEntity paiement) {
        if (StatutPaiementEnum.ANNULE == paiement.getStatut()) {
            throw new PaiementValidationException(PaiementErreurEnum.PAIEMENT_ANNULE_NON_MODIFIABLE,
                    "Le paiement " + paiement.getId() + " est annulé : il ne peut plus être modifié");
        }
    }

    private void validerDate(LocalDate datePaiement) {
        if (datePaiement == null) {
            throw new PaiementValidationException(PaiementErreurEnum.DATE_OBLIGATOIRE,
                    "La date du paiement est obligatoire");
        }
        if (datePaiement.isAfter(LocalDate.now(this.clock))) {
            throw new PaiementValidationException(PaiementErreurEnum.DATE_FUTURE,
                    "La date du paiement ne peut pas être postérieure à aujourd'hui");
        }
    }

    private void validerMode(ModePaiementEnum mode) {
        if (mode == null) {
            throw new PaiementValidationException(PaiementErreurEnum.MODE_OBLIGATOIRE,
                    "Le mode de paiement est obligatoire");
        }
        if (!mode.isSaisissableManuellement()) {
            throw new PaiementValidationException(PaiementErreurEnum.MODE_WEB_NON_AUTORISE,
                    "Le mode " + mode + " est réservé au règlement en ligne");
        }
    }

    /**
     * @param montantRegleHorsCourant montant déjà encaissé, hors paiement en cours de saisie ou de
     *                                modification
     */
    private void validerMontant(BigDecimal montant, BigDecimal montantDu, BigDecimal montantRegleHorsCourant) {
        if (montant == null || montant.signum() <= 0) {
            throw new PaiementValidationException(PaiementErreurEnum.MONTANT_INVALIDE,
                    "Le montant du paiement doit être strictement positif");
        }
        BigDecimal resteAPayer = montantDu.subtract(montantRegleHorsCourant);
        if (montant.compareTo(resteAPayer) > 0) {
            throw new PaiementValidationException(PaiementErreurEnum.MONTANT_SUPERIEUR_RESTE,
                    "Le montant du paiement (" + montant + ") dépasse le reste à payer (" + resteAPayer + ")");
        }
    }

}
