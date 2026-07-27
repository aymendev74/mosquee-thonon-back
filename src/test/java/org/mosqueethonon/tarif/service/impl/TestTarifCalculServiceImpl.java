package org.mosqueethonon.tarif.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.config.TimeConfiguration;
import org.mosqueethonon.common.security.context.SecurityContext;
import org.mosqueethonon.inscription.entity.InscriptionAdulteEntity;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
import org.mosqueethonon.inscription.enums.StatutProfessionnelEnum;
import org.mosqueethonon.inscription.repository.InscriptionAdulteRepository;
import org.mosqueethonon.inscription.repository.InscriptionRepository;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantInfosDto;
import org.mosqueethonon.param.service.ParamService;
import org.mosqueethonon.referentiel.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.tarif.criteria.TarifCriteria;
import org.mosqueethonon.tarif.enums.TypeTarifEnum;
import org.mosqueethonon.tarif.service.TarifService;
import org.mosqueethonon.tarif.v1.dto.TarifDto;
import org.mosqueethonon.tarif.v1.dto.TarifInscriptionAdulteDto;
import org.mosqueethonon.tarif.v1.dto.TarifInscriptionEnfantDto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestTarifCalculServiceImpl {

    private static final Long ID_PERIODE = 7L;

    @Mock
    private TarifService tarifService;

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private InscriptionAdulteRepository inscriptionAdulteRepository;

    @Mock
    private ParamService paramService;

    @Mock
    private SecurityContext securityContext;

    // Horloge réelle sur le fuseau de l'application : comportement identique à avant
    // l'injection du Clock. Utiliser Clock.fixed(...) pour un test sensible à la date.
    @Spy
    private Clock clock = Clock.system(TimeConfiguration.ZONE_APPLICATION);

    @InjectMocks
    private TarifCalculServiceImpl underTest;

    private PeriodeInfoDto periode(int nbMaxInscription) {
        PeriodeInfoDto periode = new PeriodeInfoDto();
        periode.setId(ID_PERIODE);
        periode.setNbMaxInscription(nbMaxInscription);
        return periode;
    }

    private TarifDto tarif(Long id, BigDecimal montant, PeriodeInfoDto periode) {
        TarifDto tarif = new TarifDto();
        tarif.setId(id);
        tarif.setMontant(montant);
        tarif.setPeriode(periode);
        return tarif;
    }

    private InscriptionEnfantInfosDto infos(boolean adherent, int nbEleves) {
        InscriptionEnfantInfosDto infos = new InscriptionEnfantInfosDto();
        infos.setAdherent(adherent);
        infos.setNbEleves(nbEleves);
        return infos;
    }

    /** Renvoie les tarifs BASE puis ENFANT, dans l'ordre des deux appels du service. */
    private void givenTarifsEnfant(List<TarifDto> base, List<TarifDto> enfant) {
        when(this.tarifService.findTarifByCriteria(any(TarifCriteria.class)))
                .thenReturn(base).thenReturn(enfant);
    }

    @Nested
    class QuandOnCalculeUnTarifEnfant {

        @Test
        public void testRetourneNullSiInscriptionsFermeesEtUtilisateurNonAdmin() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(false);
            when(paramService.isInscriptionEnfantEnabled()).thenReturn(false);

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(false, 1));

            // THEN
            assertNull(result);
            Mockito.verifyNoInteractions(tarifService);
        }

        @Test
        public void testCalculeMemeInscriptionsFermeesQuandAdmin() {
            // GIVEN — l'admin court-circuite le paramètre d'ouverture
            when(securityContext.isAdmin()).thenReturn(true);
            PeriodeInfoDto periode = periode(100);
            givenTarifsEnfant(List.of(tarif(1L, new BigDecimal("30"), periode)),
                    List.of(tarif(2L, new BigDecimal("15"), periode)));
            when(inscriptionRepository.getNbElevesInscritsByIdPeriode(Mockito.eq(ID_PERIODE), Mockito.anyString()))
                    .thenReturn(0);

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(false, 1));

            // THEN
            assertEquals(new BigDecimal("30"), result.getTarifBase());
            Mockito.verifyNoInteractions(paramService);
        }

        @Test
        public void testRetourneLesDeuxTarifsEtLeurIdentifiant() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(false);
            when(paramService.isInscriptionEnfantEnabled()).thenReturn(true);
            PeriodeInfoDto periode = periode(100);
            givenTarifsEnfant(List.of(tarif(11L, new BigDecimal("30"), periode)),
                    List.of(tarif(22L, new BigDecimal("15"), periode)));
            when(inscriptionRepository.getNbElevesInscritsByIdPeriode(Mockito.eq(ID_PERIODE), Mockito.anyString()))
                    .thenReturn(10);

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(true, 2));

            // THEN
            assertEquals(new BigDecimal("30"), result.getTarifBase());
            assertEquals(11L, result.getIdTariBase());
            assertEquals(new BigDecimal("15"), result.getTarifEleve());
            assertEquals(22L, result.getIdTariEleve());
        }

        @Test
        public void testRetourneNullSiAucunTarifDeBase() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(true);
            when(tarifService.findTarifByCriteria(any(TarifCriteria.class))).thenReturn(Collections.emptyList());

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(false, 1));

            // THEN
            assertNull(result);
        }

        @Test
        public void testRetourneNullSiTarifDeBaseTrouveMaisPasDeTarifEnfant() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(true);
            givenTarifsEnfant(List.of(tarif(1L, new BigDecimal("30"), periode(100))), Collections.emptyList());

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(false, 1));

            // THEN
            assertNull(result);
        }

        @Test
        public void testSignaleLaListeDAttenteQuandLeMaximumEstDepasse() {
            // GIVEN — 98 inscrits + 3 nouveaux > 100 places
            when(securityContext.isAdmin()).thenReturn(true);
            PeriodeInfoDto periode = periode(100);
            givenTarifsEnfant(List.of(tarif(1L, new BigDecimal("30"), periode)),
                    List.of(tarif(2L, new BigDecimal("15"), periode)));
            when(inscriptionRepository.getNbElevesInscritsByIdPeriode(Mockito.eq(ID_PERIODE), Mockito.anyString()))
                    .thenReturn(98);

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(false, 3));

            // THEN
            assertTrue(result.isListeAttente());
        }

        @Test
        public void testPasDeListeDAttenteQuandLeMaximumEstExactementAtteint() {
            // GIVEN — 98 inscrits + 2 nouveaux = 100 places, la limite n'est pas dépassée
            when(securityContext.isAdmin()).thenReturn(true);
            PeriodeInfoDto periode = periode(100);
            givenTarifsEnfant(List.of(tarif(1L, new BigDecimal("30"), periode)),
                    List.of(tarif(2L, new BigDecimal("15"), periode)));
            when(inscriptionRepository.getNbElevesInscritsByIdPeriode(Mockito.eq(ID_PERIODE), Mockito.anyString()))
                    .thenReturn(98);

            // WHEN
            TarifInscriptionEnfantDto result = underTest.calculTarifInscriptionEnfant(null, infos(false, 2));

            // THEN
            assertFalse(result.isListeAttente());
        }

        @Test
        public void testSeBaseSurLaDateDeLInscriptionExistante() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(true);
            InscriptionEnfantEntity inscription = new InscriptionEnfantEntity();
            inscription.setDateInscription(LocalDateTime.of(2024, 3, 15, 10, 0));
            when(inscriptionRepository.findById(5L)).thenReturn(Optional.of(inscription));
            PeriodeInfoDto periode = periode(100);
            givenTarifsEnfant(List.of(tarif(1L, new BigDecimal("30"), periode)),
                    List.of(tarif(2L, new BigDecimal("15"), periode)));
            when(inscriptionRepository.getNbElevesInscritsByIdPeriode(Mockito.eq(ID_PERIODE), Mockito.anyString()))
                    .thenReturn(0);

            // WHEN
            underTest.calculTarifInscriptionEnfant(5L, infos(false, 1));

            // THEN — les critères portent la date de l'inscription, pas celle du jour
            ArgumentCaptorHelper.assertAtDate(tarifService, LocalDate.of(2024, 3, 15));
        }

        @Test
        public void testLeveUneExceptionSiLInscriptionEstIntrouvable() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(true);
            when(inscriptionRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> underTest.calculTarifInscriptionEnfant(404L, infos(false, 1)));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
        }
    }

    @Nested
    class QuandOnCalculeUnTarifAdulte {

        @Test
        public void testRetourneLeTarifCorrespondantAuStatutProfessionnel() {
            // GIVEN
            when(tarifService.findTarifByCriteria(any(TarifCriteria.class)))
                    .thenReturn(List.of(tarif(9L, new BigDecimal("50"), null)));

            // WHEN
            TarifInscriptionAdulteDto result = underTest.calculTarifInscriptionAdulte(
                    null, LocalDate.of(2024, 9, 1), StatutProfessionnelEnum.ETUDIANT);

            // THEN
            assertEquals(9L, result.getIdTari());
            assertEquals(new BigDecimal("50"), result.getTarif());
        }

        @Test
        public void testTraduitLeStatutProfessionnelEnTypeDeTarif() {
            // GIVEN
            when(tarifService.findTarifByCriteria(any(TarifCriteria.class)))
                    .thenReturn(List.of(tarif(9L, new BigDecimal("120"), null)));

            // WHEN
            underTest.calculTarifInscriptionAdulte(null, LocalDate.of(2024, 9, 1),
                    StatutProfessionnelEnum.AVEC_ACTIVITE);

            // THEN
            ArgumentCaptorHelper.assertType(tarifService, TypeTarifEnum.AVEC_ACTIVITE);
        }

        @Test
        public void testRetourneNullQuandAucunTarifNeCorrespond() {
            // GIVEN
            when(tarifService.findTarifByCriteria(any(TarifCriteria.class))).thenReturn(Collections.emptyList());

            // WHEN
            TarifInscriptionAdulteDto result = underTest.calculTarifInscriptionAdulte(
                    null, LocalDate.of(2024, 9, 1), StatutProfessionnelEnum.ETUDIANT);

            // THEN
            assertNull(result);
        }

        @Test
        public void testSeBaseSurLaDateDeLInscriptionExistante() {
            // GIVEN
            InscriptionAdulteEntity inscription = new InscriptionAdulteEntity();
            inscription.setDateInscription(LocalDateTime.of(2023, 11, 2, 8, 30));
            when(inscriptionAdulteRepository.findById(3L)).thenReturn(Optional.of(inscription));
            when(tarifService.findTarifByCriteria(any(TarifCriteria.class)))
                    .thenReturn(List.of(tarif(9L, new BigDecimal("50"), null)));

            // WHEN — la date passée en paramètre doit être ignorée au profit de celle de l'inscription
            underTest.calculTarifInscriptionAdulte(3L, LocalDate.of(2030, 1, 1),
                    StatutProfessionnelEnum.ETUDIANT);

            // THEN
            ArgumentCaptorHelper.assertAtDate(tarifService, LocalDate.of(2023, 11, 2));
        }

        @Test
        public void testLeveUneExceptionSiLInscriptionEstIntrouvable() {
            // GIVEN
            when(inscriptionAdulteRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> underTest.calculTarifInscriptionAdulte(404L, LocalDate.now(clock),
                            StatutProfessionnelEnum.ETUDIANT));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
        }
    }

    /** Capture les critères passés au TarifService pour vérifier ce que le calcul a réellement demandé. */
    private static final class ArgumentCaptorHelper {

        static void assertAtDate(TarifService tarifService, LocalDate expected) {
            org.mockito.ArgumentCaptor<TarifCriteria> captor =
                    org.mockito.ArgumentCaptor.forClass(TarifCriteria.class);
            Mockito.verify(tarifService, Mockito.atLeastOnce()).findTarifByCriteria(captor.capture());
            assertTrue(captor.getAllValues().stream().allMatch(c -> expected.equals(c.getAtDate())),
                    "tous les critères doivent porter la date " + expected);
        }

        static void assertType(TarifService tarifService, TypeTarifEnum expected) {
            org.mockito.ArgumentCaptor<TarifCriteria> captor =
                    org.mockito.ArgumentCaptor.forClass(TarifCriteria.class);
            Mockito.verify(tarifService).findTarifByCriteria(captor.capture());
            assertEquals(expected, captor.getValue().getType());
        }
    }
}
