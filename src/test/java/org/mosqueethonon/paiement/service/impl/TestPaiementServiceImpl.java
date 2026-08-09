package org.mosqueethonon.paiement.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.config.TimeConfiguration;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
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
import org.mosqueethonon.paiement.v1.dto.PaiementDto;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;
import org.mosqueethonon.paiement.v1.mapper.PaiementMapper;
import org.mosqueethonon.paiement.v1.mapper.PaiementMapperImpl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestPaiementServiceImpl {

    private static final Long ID_INSCRIPTION = 42L;

    private static final Clock HORLOGE_FIGEE = Clock.fixed(
            LocalDate.of(2026, Month.SEPTEMBER, 15).atStartOfDay(TimeConfiguration.ZONE_APPLICATION).toInstant(),
            TimeConfiguration.ZONE_APPLICATION);
    private static final LocalDate AUJOURD_HUI = LocalDate.now(HORLOGE_FIGEE);

    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private InscriptionRepository inscriptionRepository;
    @Spy
    private PaiementMapper paiementMapper = new PaiementMapperImpl();
    @Spy
    private Clock clock = HORLOGE_FIGEE;

    @InjectMocks
    private PaiementServiceImpl underTest;

    // ---------------------------------------------------------------------------------------
    // Situation
    // ---------------------------------------------------------------------------------------

    @Test
    public void testGetSituationSansPaiementRetourneNonRegle() {
        // GIVEN une inscription de 200 € sans aucun paiement
        this.givenInscription(bd(200));
        this.givenMontantRegle(null);
        this.givenPaiements();

        // WHEN on demande la situation
        SituationPaiementDto situation = this.underTest.getSituation(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION);

        // THEN rien n'est réglé et tout reste à payer
        assertEquals(0, bd(200).compareTo(situation.getMontantTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(situation.getMontantRegle()));
        assertEquals(0, bd(200).compareTo(situation.getResteAPayer()));
        assertEquals(StatutReglementEnum.NON_REGLE, situation.getStatutReglement());
        assertTrue(situation.getPaiements().isEmpty());
    }

    @Test
    public void testGetSituationPartiellementRegleeRetournePartiel() {
        // GIVEN une inscription de 200 € réglée à hauteur de 120 €
        this.givenInscription(bd(200));
        this.givenMontantRegle(bd(120));
        this.givenPaiements(this.paiementValide(1L, bd(120)));

        // WHEN on demande la situation
        SituationPaiementDto situation = this.underTest.getSituation(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION);

        // THEN il reste 80 € à payer
        assertEquals(0, bd(80).compareTo(situation.getResteAPayer()));
        assertEquals(StatutReglementEnum.PARTIEL, situation.getStatutReglement());
        assertEquals(1, situation.getPaiements().size());
    }

    @Test
    public void testGetSituationMontantTotalNulRetourneSolde() {
        // GIVEN une inscription dont le montant dû est nul
        this.givenInscription(BigDecimal.ZERO);
        this.givenMontantRegle(null);
        this.givenPaiements();

        // WHEN on demande la situation
        SituationPaiementDto situation = this.underTest.getSituation(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION);

        // THEN elle est considérée comme soldée
        assertEquals(StatutReglementEnum.SOLDE, situation.getStatutReglement());
    }

    @Test
    public void testGetSituationMontantTotalAbsentEstTraiteCommeZero() {
        // GIVEN une inscription dont le montant total n'a jamais été calculé
        this.givenInscription(null);
        this.givenMontantRegle(null);
        this.givenPaiements();

        // WHEN on demande la situation
        SituationPaiementDto situation = this.underTest.getSituation(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION);

        // THEN le montant dû vaut zéro plutôt que de faire échouer l'appel
        assertEquals(0, BigDecimal.ZERO.compareTo(situation.getMontantTotal()));
        assertEquals(StatutReglementEnum.SOLDE, situation.getStatutReglement());
    }

    @Test
    public void testGetSituationEncaisseeAuDelaDuDuRetourneTropPercu() {
        // GIVEN une inscription de 100 € sur laquelle 120 € ont été encaissés — cas atteignable
        // uniquement si le tarif a été revu à la baisse après encaissement
        this.givenInscription(bd(100));
        this.givenMontantRegle(bd(120));
        this.givenPaiements(this.paiementValide(1L, bd(120)));

        // WHEN on demande la situation
        SituationPaiementDto situation = this.underTest.getSituation(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION);

        // THEN le trop-perçu est signalé et le reste à payer est négatif
        assertEquals(StatutReglementEnum.TROP_PERCU, situation.getStatutReglement());
        assertEquals(0, bd(-20).compareTo(situation.getResteAPayer()));
    }

    @Test
    public void testGetSituationCibleInexistanteEchoue() {
        // GIVEN une inscription qui n'existe pas
        when(this.inscriptionRepository.findById(ID_INSCRIPTION)).thenReturn(Optional.empty());

        // WHEN / THEN la cible est signalée comme introuvable
        this.assertErreur(PaiementErreurEnum.CIBLE_INTROUVABLE,
                () -> this.underTest.getSituation(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION));
    }

    // ---------------------------------------------------------------------------------------
    // Création
    // ---------------------------------------------------------------------------------------

    @Test
    public void testCreerPaiementPartielEnregistreUnPaiementValide() {
        // GIVEN une inscription de 200 € sans paiement
        this.givenInscription(bd(200));
        this.givenMontantRegle(null, bd(120));
        this.givenPaiements(this.paiementValide(1L, bd(120)));

        // WHEN on saisit un règlement de 120 € en espèces
        SituationPaiementDto situation = this.underTest.creer(this.dto(bd(120), ModePaiementEnum.ESPECE));

        // THEN le paiement est persisté au statut VALIDE et la situation renvoyée est à jour
        ArgumentCaptor<PaiementEntity> captor = ArgumentCaptor.forClass(PaiementEntity.class);
        verify(this.paiementRepository).save(captor.capture());
        PaiementEntity enregistre = captor.getValue();
        assertEquals(StatutPaiementEnum.VALIDE, enregistre.getStatut());
        assertEquals(0, bd(120).compareTo(enregistre.getMontant()));
        assertEquals(ModePaiementEnum.ESPECE, enregistre.getMode());
        assertEquals(TypeCiblePaiementEnum.INSCRIPTION, enregistre.getTypeCible());
        assertEquals(ID_INSCRIPTION, enregistre.getIdCible());
        assertEquals(StatutReglementEnum.PARTIEL, situation.getStatutReglement());
    }

    @Test
    public void testCreerPaiementSoldantRetourneSolde() {
        // GIVEN une inscription de 200 € déjà réglée à 120 €
        this.givenInscription(bd(200));
        this.givenMontantRegle(bd(120), bd(200));
        this.givenPaiements(this.paiementValide(1L, bd(120)), this.paiementValide(2L, bd(80)));

        // WHEN on saisit le solde de 80 €
        SituationPaiementDto situation = this.underTest.creer(this.dto(bd(80), ModePaiementEnum.CHEQUE));

        // THEN l'inscription est soldée
        assertEquals(StatutReglementEnum.SOLDE, situation.getStatutReglement());
        assertEquals(0, BigDecimal.ZERO.compareTo(situation.getResteAPayer()));
    }

    @Test
    public void testCreerPaiementSuperieurAuResteEchoue() {
        // GIVEN une inscription de 200 € déjà réglée à 120 €
        this.givenInscription(bd(200));
        this.givenMontantRegle(bd(120));

        // WHEN / THEN un règlement de 150 € est refusé
        this.assertErreur(PaiementErreurEnum.MONTANT_SUPERIEUR_RESTE,
                () -> this.underTest.creer(this.dto(bd(150), ModePaiementEnum.ESPECE)));
        verify(this.paiementRepository, never()).save(any());
    }

    @Test
    public void testCreerPaiementMontantNegatifEchoue() {
        // GIVEN une inscription de 200 € sans paiement
        this.givenInscription(bd(200));
        this.givenMontantRegle(null);

        // WHEN / THEN un montant négatif est refusé
        this.assertErreur(PaiementErreurEnum.MONTANT_INVALIDE,
                () -> this.underTest.creer(this.dto(bd(-10), ModePaiementEnum.ESPECE)));
    }

    @Test
    public void testCreerPaiementMontantNulEchoue() {
        // GIVEN une inscription de 200 € sans paiement
        this.givenInscription(bd(200));
        this.givenMontantRegle(null);

        // WHEN / THEN un montant à zéro est refusé
        this.assertErreur(PaiementErreurEnum.MONTANT_INVALIDE,
                () -> this.underTest.creer(this.dto(BigDecimal.ZERO, ModePaiementEnum.ESPECE)));
    }

    @Test
    public void testCreerPaiementSansDateEchoue() {
        // GIVEN une inscription de 200 €
        this.givenInscription(bd(200));

        // WHEN / THEN un paiement sans date est refusé
        PaiementDto paiement = this.dto(bd(50), ModePaiementEnum.ESPECE);
        paiement.setDatePaiement(null);
        this.assertErreur(PaiementErreurEnum.DATE_OBLIGATOIRE, () -> this.underTest.creer(paiement));
    }

    @Test
    public void testCreerPaiementDateFutureEchoue() {
        // GIVEN une inscription de 200 €
        this.givenInscription(bd(200));

        // WHEN / THEN un paiement daté de demain est refusé
        PaiementDto paiement = this.dto(bd(50), ModePaiementEnum.ESPECE);
        paiement.setDatePaiement(AUJOURD_HUI.plusDays(1));
        this.assertErreur(PaiementErreurEnum.DATE_FUTURE, () -> this.underTest.creer(paiement));
    }

    @Test
    public void testCreerPaiementDuJourEstAccepte() {
        // GIVEN une inscription de 200 € sans paiement
        this.givenInscription(bd(200));
        this.givenMontantRegle(null, bd(50));
        this.givenPaiements(this.paiementValide(1L, bd(50)));

        // WHEN on saisit un paiement daté d'aujourd'hui
        PaiementDto paiement = this.dto(bd(50), ModePaiementEnum.ESPECE);
        paiement.setDatePaiement(AUJOURD_HUI);
        this.underTest.creer(paiement);

        // THEN il est accepté — la borne du jour est incluse
        verify(this.paiementRepository).save(any());
    }

    @Test
    public void testCreerPaiementSansModeEchoue() {
        // GIVEN une inscription de 200 €
        this.givenInscription(bd(200));

        // WHEN / THEN un paiement sans mode est refusé
        this.assertErreur(PaiementErreurEnum.MODE_OBLIGATOIRE,
                () -> this.underTest.creer(this.dto(bd(50), null)));
    }

    @Test
    public void testCreerPaiementModeWebEchoue() {
        // GIVEN une inscription de 200 €
        this.givenInscription(bd(200));

        // WHEN / THEN le mode réservé au règlement en ligne est refusé en saisie manuelle
        this.assertErreur(PaiementErreurEnum.MODE_WEB_NON_AUTORISE,
                () -> this.underTest.creer(this.dto(bd(50), ModePaiementEnum.WEB)));
    }

    @Test
    public void testCreerPaiementSurCibleInexistanteEchoue() {
        // GIVEN une inscription qui n'existe pas
        when(this.inscriptionRepository.findById(ID_INSCRIPTION)).thenReturn(Optional.empty());

        // WHEN / THEN la cible est signalée comme introuvable
        this.assertErreur(PaiementErreurEnum.CIBLE_INTROUVABLE,
                () -> this.underTest.creer(this.dto(bd(50), ModePaiementEnum.ESPECE)));
    }

    // ---------------------------------------------------------------------------------------
    // Modification
    // ---------------------------------------------------------------------------------------

    @Test
    public void testModifierPaiementAugmenteLeMontantSansCompterDeuxFoisLExistant() {
        // GIVEN une inscription de 200 € réglée par un unique paiement de 120 €
        PaiementEntity existant = this.paiementValide(1L, bd(120));
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.of(existant));
        this.givenInscription(bd(200));
        this.givenMontantRegle(bd(120), bd(150));
        this.givenPaiements(existant);

        // WHEN on porte ce paiement à 150 €
        SituationPaiementDto situation = this.underTest.modifier(1L, this.dto(bd(150), ModePaiementEnum.CARTE));

        // THEN c'est accepté : le paiement modifié est retiré du déjà-réglé avant contrôle, le reste
        // à payer opposable est donc bien de 200 € et non de 80 €
        assertEquals(0, bd(150).compareTo(existant.getMontant()));
        assertEquals(ModePaiementEnum.CARTE, existant.getMode());
        verify(this.paiementRepository).save(existant);
        assertEquals(StatutReglementEnum.PARTIEL, situation.getStatutReglement());
    }

    @Test
    public void testModifierPaiementAuDelaDuResteEchoue() {
        // GIVEN une inscription de 200 € réglée par un paiement de 120 € et un autre de 50 €
        PaiementEntity existant = this.paiementValide(1L, bd(120));
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.of(existant));
        this.givenInscription(bd(200));
        this.givenMontantRegle(bd(170));

        // WHEN / THEN porter le premier à 160 € dépasserait le dû (160 + 50 > 200)
        this.assertErreur(PaiementErreurEnum.MONTANT_SUPERIEUR_RESTE,
                () -> this.underTest.modifier(1L, this.dto(bd(160), ModePaiementEnum.ESPECE)));
        verify(this.paiementRepository, never()).save(any());
    }

    @Test
    public void testModifierPaiementAnnuleEchoue() {
        // GIVEN un paiement déjà annulé
        PaiementEntity annule = this.paiementValide(1L, bd(120));
        annule.setStatut(StatutPaiementEnum.ANNULE);
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.of(annule));

        // WHEN / THEN il est figé
        this.assertErreur(PaiementErreurEnum.PAIEMENT_ANNULE_NON_MODIFIABLE,
                () -> this.underTest.modifier(1L, this.dto(bd(50), ModePaiementEnum.ESPECE)));
        verify(this.paiementRepository, never()).save(any());
    }

    @Test
    public void testModifierPaiementNeChangePasLaCible() {
        // GIVEN un paiement de 120 € rattaché à l'inscription 42
        PaiementEntity existant = this.paiementValide(1L, bd(120));
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.of(existant));
        this.givenInscription(bd(200));
        this.givenMontantRegle(bd(120), bd(120));
        this.givenPaiements(existant);

        // WHEN on tente de le rattacher à une autre inscription
        PaiementDto paiement = this.dto(bd(120), ModePaiementEnum.ESPECE);
        paiement.setIdCible(999L);
        SituationPaiementDto situation = this.underTest.modifier(1L, paiement);

        // THEN la cible d'origine est conservée
        assertEquals(ID_INSCRIPTION, existant.getIdCible());
        assertEquals(ID_INSCRIPTION, situation.getIdCible());
    }

    @Test
    public void testModifierPaiementInexistantEchoue() {
        // GIVEN un identifiant de paiement inconnu
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN / THEN la ressource est signalée absente
        assertThrows(ResourceNotFoundException.class,
                () -> this.underTest.modifier(1L, this.dto(bd(50), ModePaiementEnum.ESPECE)));
    }

    // ---------------------------------------------------------------------------------------
    // Annulation
    // ---------------------------------------------------------------------------------------

    @Test
    public void testAnnulerPaiementLeSortDuMontantRegleSansLeSupprimer() {
        // GIVEN une inscription de 200 € réglée par un paiement de 120 €
        PaiementEntity existant = this.paiementValide(1L, bd(120));
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.of(existant));
        this.givenInscription(bd(200));
        this.givenMontantRegle(null);
        this.givenPaiements(existant);

        // WHEN on annule ce paiement
        SituationPaiementDto situation = this.underTest.annuler(1L);

        // THEN il reste en base, au statut ANNULE, et le reste à payer remonte
        assertEquals(StatutPaiementEnum.ANNULE, existant.getStatut());
        verify(this.paiementRepository).save(existant);
        verify(this.paiementRepository, never()).delete(any());
        assertEquals(0, bd(200).compareTo(situation.getResteAPayer()));
        assertEquals(StatutReglementEnum.NON_REGLE, situation.getStatutReglement());
        assertEquals(1, situation.getPaiements().size());
    }

    @Test
    public void testAnnulerPaiementDejaAnnuleEchoue() {
        // GIVEN un paiement déjà annulé
        PaiementEntity annule = this.paiementValide(1L, bd(120));
        annule.setStatut(StatutPaiementEnum.ANNULE);
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.of(annule));

        // WHEN / THEN une seconde annulation est refusée
        this.assertErreur(PaiementErreurEnum.PAIEMENT_ANNULE_NON_MODIFIABLE,
                () -> this.underTest.annuler(1L));
        verify(this.paiementRepository, never()).save(any());
    }

    @Test
    public void testAnnulerPaiementInexistantEchoue() {
        // GIVEN un identifiant de paiement inconnu
        when(this.paiementRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN / THEN la ressource est signalée absente
        assertThrows(ResourceNotFoundException.class, () -> this.underTest.annuler(1L));
    }

    // ---------------------------------------------------------------------------------------
    // Utilitaires
    // ---------------------------------------------------------------------------------------

    private static BigDecimal bd(int valeur) {
        return BigDecimal.valueOf(valeur);
    }

    private void givenInscription(BigDecimal montantTotal) {
        InscriptionEntity inscription = new InscriptionEnfantEntity();
        inscription.setId(ID_INSCRIPTION);
        inscription.setMontantTotal(montantTotal);
        lenient().when(this.inscriptionRepository.findById(ID_INSCRIPTION)).thenReturn(Optional.of(inscription));
    }

    /**
     * Le montant réglé est relu après chaque mutation : les appels successifs peuvent donc renvoyer
     * des valeurs différentes.
     */
    private void givenMontantRegle(BigDecimal avant, BigDecimal... suivants) {
        lenient().when(this.paiementRepository.sumMontantValide(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION))
                .thenReturn(avant, suivants);
    }

    private void givenPaiements(PaiementEntity... paiements) {
        lenient().when(this.paiementRepository
                        .findByTypeCibleAndIdCibleOrderByDatePaiementAscIdAsc(TypeCiblePaiementEnum.INSCRIPTION, ID_INSCRIPTION))
                .thenReturn(paiements.length == 0 ? Collections.emptyList() : List.of(paiements));
    }

    private PaiementEntity paiementValide(Long id, BigDecimal montant) {
        return PaiementEntity.builder()
                .id(id)
                .typeCible(TypeCiblePaiementEnum.INSCRIPTION)
                .idCible(ID_INSCRIPTION)
                .montant(montant)
                .datePaiement(AUJOURD_HUI)
                .mode(ModePaiementEnum.ESPECE)
                .statut(StatutPaiementEnum.VALIDE)
                .build();
    }

    private PaiementDto dto(BigDecimal montant, ModePaiementEnum mode) {
        return PaiementDto.builder()
                .typeCible(TypeCiblePaiementEnum.INSCRIPTION)
                .idCible(ID_INSCRIPTION)
                .montant(montant)
                .datePaiement(AUJOURD_HUI.minusDays(1))
                .mode(mode)
                .build();
    }

    private void assertErreur(PaiementErreurEnum attendue, Executable action) {
        PaiementValidationException exception = assertThrows(PaiementValidationException.class, action);
        assertEquals(attendue.name(), exception.getCode());
    }

}
