package org.mosqueethonon.tarif.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.referentiel.entity.PeriodeEntity;
import org.mosqueethonon.referentiel.repository.PeriodeRepository;
import org.mosqueethonon.tarif.entity.TarifEntity;
import org.mosqueethonon.tarif.enums.TypeTarifEnum;
import org.mosqueethonon.tarif.repository.TarifRepository;
import org.mosqueethonon.tarif.v1.dto.InfoTarifDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class TestTarifAdminServiceImpl {

    private static final Long ID_PERIODE = 42L;

    @Mock
    private TarifRepository tarifRepository;

    @Mock
    private PeriodeRepository periodeRepository;

    @InjectMocks
    private TarifAdminServiceImpl underTest;

    @Captor
    private ArgumentCaptor<List<TarifEntity>> tarifsSauvegardes;

    private PeriodeEntity periode;

    @BeforeEach
    public void setUp() {
        this.periode = new PeriodeEntity();
        this.periode.setId(ID_PERIODE);
    }

    private void givenPeriode(String application) {
        this.periode.setApplication(application);
        when(this.periodeRepository.findById(ID_PERIODE)).thenReturn(Optional.of(this.periode));
    }

    private TarifEntity tarif(String code, TypeTarifEnum type, BigDecimal montant) {
        return TarifEntity.builder().code(code).type(type).montant(montant).periode(this.periode).build();
    }

    /** Un DTO dont les 16 montants enfant sont renseignés : Collectors.toMap refuse les valeurs nulles. */
    private InfoTarifDto.InfoTarifDtoBuilder infoTarifEnfantComplet() {
        return InfoTarifDto.builder().idPeriode(ID_PERIODE)
                .montantBase1Enfant(new BigDecimal("10")).montantBase1EnfantAdherent(new BigDecimal("11"))
                .montantEnfant1Enfant(new BigDecimal("12")).montantEnfant1EnfantAdherent(new BigDecimal("13"))
                .montantBase2Enfant(new BigDecimal("20")).montantBase2EnfantAdherent(new BigDecimal("21"))
                .montantEnfant2Enfant(new BigDecimal("22")).montantEnfant2EnfantAdherent(new BigDecimal("23"))
                .montantBase3Enfant(new BigDecimal("30")).montantBase3EnfantAdherent(new BigDecimal("31"))
                .montantEnfant3Enfant(new BigDecimal("32")).montantEnfant3EnfantAdherent(new BigDecimal("33"))
                .montantBase4Enfant(new BigDecimal("40")).montantBase4EnfantAdherent(new BigDecimal("41"))
                .montantEnfant4Enfant(new BigDecimal("42")).montantEnfant4EnfantAdherent(new BigDecimal("43"));
    }

    private InfoTarifDto.InfoTarifDtoBuilder infoTarifAdulteComplet() {
        return InfoTarifDto.builder().idPeriode(ID_PERIODE)
                .montantEtudiant(new BigDecimal("50"))
                .montantAvecActivite(new BigDecimal("120"))
                .montantSansActivite(new BigDecimal("80"));
    }

    @Nested
    class QuandOnLitLesTarifsDUnePeriode {

        @Test
        public void testLevExceptionSiPeriodeInexistante() {
            // GIVEN
            when(periodeRepository.findById(ID_PERIODE)).thenReturn(Optional.empty());

            // WHEN
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> underTest.findInfoTarifByPeriode(ID_PERIODE));

            // THEN
            assertTrue(exception.getMessage().contains(String.valueOf(ID_PERIODE)));
        }

        @Test
        public void testRetourneUnDtoVideQuandAucunTarif() {
            // GIVEN
            givenPeriode("COURS_ENFANT");
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(Collections.emptyList());

            // WHEN
            InfoTarifDto result = underTest.findInfoTarifByPeriode(ID_PERIODE);

            // THEN
            assertNotNull(result);
            assertEquals(ID_PERIODE, result.getIdPeriode());
            assertNull(result.getMontantBase1Enfant());
        }

        @Test
        public void testMappeParCodeQuandPeriodeEnfant() {
            // GIVEN
            givenPeriode("COURS_ENFANT");
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(List.of(
                    tarif("BASE_1_ENFANT", TypeTarifEnum.BASE, new BigDecimal("10")),
                    tarif("ENFANT_ADHERENT_3_ENFANT", TypeTarifEnum.ENFANT, new BigDecimal("33"))));

            // WHEN
            InfoTarifDto result = underTest.findInfoTarifByPeriode(ID_PERIODE);

            // THEN
            assertEquals(new BigDecimal("10"), result.getMontantBase1Enfant());
            assertEquals(new BigDecimal("33"), result.getMontantEnfant3EnfantAdherent());
            assertNull(result.getMontantBase2Enfant());
        }

        @Test
        public void testMappeParTypeQuandPeriodeAdulte() {
            // GIVEN
            givenPeriode("COURS_ADULTE");
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(List.of(
                    tarif(null, TypeTarifEnum.ETUDIANT, new BigDecimal("50")),
                    tarif(null, TypeTarifEnum.SANS_ACTIVITE, new BigDecimal("80"))));

            // WHEN
            InfoTarifDto result = underTest.findInfoTarifByPeriode(ID_PERIODE);

            // THEN
            assertEquals(new BigDecimal("50"), result.getMontantEtudiant());
            assertEquals(new BigDecimal("80"), result.getMontantSansActivite());
            assertNull(result.getMontantAvecActivite());
        }

        @Test
        public void testIgnoreUnCodeTarifInconnu() {
            // GIVEN
            givenPeriode("COURS_ENFANT");
            when(tarifRepository.findByPeriodeId(ID_PERIODE))
                    .thenReturn(List.of(tarif("CODE_QUI_NEXISTE_PLUS", TypeTarifEnum.BASE, new BigDecimal("99"))));

            // WHEN
            InfoTarifDto result = underTest.findInfoTarifByPeriode(ID_PERIODE);

            // THEN — aucun champ ne correspond, le DTO reste vide sans lever d'erreur
            assertNull(result.getMontantBase1Enfant());
        }
    }

    @Nested
    class QuandOnEnregistreLesTarifsEnfant {

        @Test
        public void testCreeLes16TarifsQuandLaPeriodeNenAvaitAucun() {
            // GIVEN
            givenPeriode("COURS_ENFANT");
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(Collections.emptyList());

            // WHEN
            underTest.saveInfoTarif(infoTarifEnfantComplet().build());

            // THEN
            Mockito.verify(tarifRepository).saveAll(tarifsSauvegardes.capture());
            List<TarifEntity> tarifs = tarifsSauvegardes.getValue();
            assertEquals(16, tarifs.size());
            assertTrue(tarifs.stream().allMatch(t -> t.getPeriode() == periode));
            TarifEntity base1 = tarifs.stream().filter(t -> "BASE_1_ENFANT".equals(t.getCode()))
                    .findFirst().orElseThrow();
            assertEquals(new BigDecimal("10"), base1.getMontant());
            assertEquals(TypeTarifEnum.BASE, base1.getType());
            assertEquals(1, base1.getNbEnfant());
            assertEquals(Boolean.FALSE, base1.getAdherent());
        }

        @Test
        public void testMetAJourLesMontantsDesTarifsExistants() {
            // GIVEN
            givenPeriode("COURS_ENFANT");
            TarifEntity existant = tarif("BASE_1_ENFANT", TypeTarifEnum.BASE, new BigDecimal("1"));
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(List.of(existant));

            // WHEN
            underTest.saveInfoTarif(infoTarifEnfantComplet().build());

            // THEN — l'entité existante est réutilisée, pas recréée
            Mockito.verify(tarifRepository).saveAll(tarifsSauvegardes.capture());
            assertEquals(1, tarifsSauvegardes.getValue().size());
            assertEquals(new BigDecimal("10"), existant.getMontant());
        }
    }

    /**
     * Le calcul d'un tarif d'inscription recherche les tarifs par le triplet
     * (type, adherent, nbEnfant) et abandonne si aucun ne correspond. Les tarifs créés
     * ici à partir des annotations de {@link InfoTarifDto} doivent donc couvrir toutes
     * les combinaisons, une seule fois chacune.
     */
    @Nested
    class QuandOnVerifieLaCoherenceDesTarifsEnfantCrees {

        private List<TarifEntity> creerLesTarifs() {
            givenPeriode("COURS_ENFANT");
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(Collections.emptyList());

            underTest.saveInfoTarif(infoTarifEnfantComplet().build());

            Mockito.verify(tarifRepository).saveAll(tarifsSauvegardes.capture());
            return tarifsSauvegardes.getValue();
        }

        private TarifEntity tarifDuCode(List<TarifEntity> tarifs, String code) {
            return tarifs.stream().filter(t -> code.equals(t.getCode())).findFirst()
                    .orElseThrow(() -> new AssertionError("Aucun tarif créé pour le code " + code));
        }

        @Test
        public void testLeTarifParEnfantPour4EnfantsEstDeTypeEnfant() {
            // Régression historique : ce tarif était créé avec type=BASE. Aucun tarif
            // (ENFANT, non adhérent, 4 enfants) n'existait alors en base, et le calcul du
            // tarif d'une inscription à 4 enfants renvoyait null — inscription impossible.
            // WHEN
            TarifEntity tarif = tarifDuCode(creerLesTarifs(), "ENFANT_4_ENFANT");

            // THEN
            assertEquals(TypeTarifEnum.ENFANT, tarif.getType());
            assertEquals(4, tarif.getNbEnfant());
            assertEquals(Boolean.FALSE, tarif.getAdherent());
        }

        @Test
        public void testLesQuatreTarifsDe4EnfantsSontCorrectementTypes() {
            // WHEN
            List<TarifEntity> tarifs = creerLesTarifs();

            // THEN
            assertEquals(TypeTarifEnum.BASE, tarifDuCode(tarifs, "BASE_4_ENFANT").getType());
            assertEquals(TypeTarifEnum.BASE, tarifDuCode(tarifs, "BASE_ADHERENT_4_ENFANT").getType());
            assertEquals(TypeTarifEnum.ENFANT, tarifDuCode(tarifs, "ENFANT_4_ENFANT").getType());
            assertEquals(TypeTarifEnum.ENFANT, tarifDuCode(tarifs, "ENFANT_ADHERENT_4_ENFANT").getType());
        }

        @Test
        public void testLesTarifsSontRepartisEnHuitBaseEtHuitParEnfant() {
            // WHEN
            List<TarifEntity> tarifs = creerLesTarifs();

            // THEN
            assertEquals(8, tarifs.stream().filter(t -> t.getType() == TypeTarifEnum.BASE).count());
            assertEquals(8, tarifs.stream().filter(t -> t.getType() == TypeTarifEnum.ENFANT).count());
        }

        @Test
        public void testChaqueCombinaisonRecherchableAuCalculExisteUneSeuleFois() {
            // WHEN
            List<TarifEntity> tarifs = creerLesTarifs();

            // THEN — aucun doublon : sinon le tarif retenu au calcul devient arbitraire
            Set<String> combinaisons = new HashSet<>();
            for (TarifEntity tarif : tarifs) {
                String combinaison = tarif.getType() + "|" + tarif.getAdherent() + "|" + tarif.getNbEnfant();
                assertTrue(combinaisons.add(combinaison),
                        "Deux tarifs partagent la combinaison " + combinaison + " (code " + tarif.getCode() + ")");
            }

            // THEN — et aucun trou : sinon le calcul renvoie null pour ce cas
            for (TypeTarifEnum type : List.of(TypeTarifEnum.BASE, TypeTarifEnum.ENFANT)) {
                for (Boolean adherent : List.of(Boolean.FALSE, Boolean.TRUE)) {
                    for (int nbEnfant = 1; nbEnfant <= 4; nbEnfant++) {
                        String combinaison = type + "|" + adherent + "|" + nbEnfant;
                        assertTrue(combinaisons.contains(combinaison), "Combinaison de tarif absente : " + combinaison);
                    }
                }
            }
        }

        @Test
        public void testChaqueMontantSaisiAtterritSurUnSeulCodeTarif() {
            // WHEN
            List<TarifEntity> tarifs = creerLesTarifs();

            // THEN
            assertEquals(new BigDecimal("42"), tarifDuCode(tarifs, "ENFANT_4_ENFANT").getMontant());
            assertEquals(new BigDecimal("40"), tarifDuCode(tarifs, "BASE_4_ENFANT").getMontant());
            assertEquals(16, tarifs.stream().map(TarifEntity::getMontant).distinct().count());
        }
    }

    @Nested
    class QuandOnEnregistreLesTarifsAdulte {

        @Test
        public void testCreeLes3TarifsQuandLaPeriodeNenAvaitAucun() {
            // GIVEN
            givenPeriode("COURS_ADULTE");
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(Collections.emptyList());

            // WHEN
            underTest.saveInfoTarif(infoTarifAdulteComplet().build());

            // THEN
            Mockito.verify(tarifRepository).saveAll(tarifsSauvegardes.capture());
            List<TarifEntity> tarifs = tarifsSauvegardes.getValue();
            assertEquals(3, tarifs.size());
            TarifEntity etudiant = tarifs.stream().filter(t -> t.getType() == TypeTarifEnum.ETUDIANT)
                    .findFirst().orElseThrow();
            assertEquals(new BigDecimal("50"), etudiant.getMontant());
        }

        @Test
        public void testMetAJourLesMontantsDesTarifsExistants() {
            // GIVEN
            givenPeriode("COURS_ADULTE");
            TarifEntity existant = tarif(null, TypeTarifEnum.AVEC_ACTIVITE, new BigDecimal("1"));
            when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(List.of(existant));

            // WHEN
            underTest.saveInfoTarif(infoTarifAdulteComplet().build());

            // THEN
            Mockito.verify(tarifRepository).saveAll(anyList());
            assertEquals(new BigDecimal("120"), existant.getMontant());
        }

        @Test
        public void testRelitLesTarifsApresEnregistrement() {
            // GIVEN
            givenPeriode("COURS_ADULTE");
            when(tarifRepository.findByPeriodeId(ID_PERIODE))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(List.of(tarif(null, TypeTarifEnum.ETUDIANT, new BigDecimal("50"))));

            // WHEN
            InfoTarifDto result = underTest.saveInfoTarif(infoTarifAdulteComplet().build());

            // THEN — la valeur retournée provient de la relecture, pas du DTO d'entrée
            assertEquals(new BigDecimal("50"), result.getMontantEtudiant());
            assertNull(result.getMontantSansActivite());
        }
    }
}
