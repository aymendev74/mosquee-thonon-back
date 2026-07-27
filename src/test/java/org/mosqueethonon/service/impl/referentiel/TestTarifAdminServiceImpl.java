package org.mosqueethonon.service.impl.referentiel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.v1.dto.referentiel.InfoTarifDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Vérifie les tarifs réellement créés en base à l'ouverture d'une période, à partir
 * des annotations de {@link InfoTarifDto}. C'est l'étape où une annotation erronée
 * se matérialise en donnée corrompue.
 */
@ExtendWith(MockitoExtension.class)
public class TestTarifAdminServiceImpl {

    private static final Long ID_PERIODE = 1L;

    @Mock
    private TarifRepository tarifRepository;

    @Mock
    private PeriodeRepository periodeRepository;

    @InjectMocks
    private TarifAdminServiceImpl underTest;

    private PeriodeEntity periodeCoursEnfant;

    @BeforeEach
    public void setUp() {
        periodeCoursEnfant = PeriodeEntity.builder().id(ID_PERIODE).application("COURS_ENFANT").build();
    }

    /**
     * Grille tarifaire complète. Tous les montants doivent être renseignés : le service
     * les indexe via {@code Collectors.toMap}, qui rejette les valeurs nulles.
     * Chaque montant est distinct pour pouvoir vérifier qu'il atterrit sur le bon code.
     */
    private InfoTarifDto grilleTarifaireComplete() {
        return InfoTarifDto.builder().idPeriode(ID_PERIODE)
                .montantBase1Enfant(BigDecimal.valueOf(101))
                .montantBase1EnfantAdherent(BigDecimal.valueOf(102))
                .montantEnfant1Enfant(BigDecimal.valueOf(103))
                .montantEnfant1EnfantAdherent(BigDecimal.valueOf(104))
                .montantBase2Enfant(BigDecimal.valueOf(105))
                .montantBase2EnfantAdherent(BigDecimal.valueOf(106))
                .montantEnfant2Enfant(BigDecimal.valueOf(107))
                .montantEnfant2EnfantAdherent(BigDecimal.valueOf(108))
                .montantBase3Enfant(BigDecimal.valueOf(109))
                .montantBase3EnfantAdherent(BigDecimal.valueOf(110))
                .montantEnfant3Enfant(BigDecimal.valueOf(111))
                .montantEnfant3EnfantAdherent(BigDecimal.valueOf(112))
                .montantBase4Enfant(BigDecimal.valueOf(165))
                .montantBase4EnfantAdherent(BigDecimal.valueOf(114))
                .montantEnfant4Enfant(BigDecimal.valueOf(12))
                .montantEnfant4EnfantAdherent(BigDecimal.valueOf(116))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<TarifEntity> capturerTarifsSauvegardes() {
        ArgumentCaptor<List<TarifEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(tarifRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    /**
     * Déclenche la création des tarifs d'une période enfant vierge et retourne
     * les entités transmises au repository.
     */
    private List<TarifEntity> creerLesTarifsDUnePeriodeEnfant() {
        when(periodeRepository.findById(ID_PERIODE)).thenReturn(Optional.of(periodeCoursEnfant));
        // Période vierge : aucun tarif existant, le service doit donc les créer
        when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(Collections.emptyList());

        underTest.saveInfoTarif(grilleTarifaireComplete());

        return capturerTarifsSauvegardes();
    }

    private TarifEntity tarifDuCode(List<TarifEntity> tarifs, String code) {
        return tarifs.stream().filter(tarif -> code.equals(tarif.getCode())).findFirst()
                .orElseThrow(() -> new AssertionError("Aucun tarif créé pour le code " + code));
    }

    @Test
    public void testLeTarifParEnfantPour4EnfantsEstCreeAvecLeTypeEnfant() {
        // Régression historique : ce tarif était créé avec type=BASE. En base, il n'existait
        // alors aucun tarif (ENFANT, non adhérent, 4 enfants), et le calcul du tarif d'une
        // inscription à 4 enfants renvoyait null — inscription impossible.
        TarifEntity tarif = tarifDuCode(creerLesTarifsDUnePeriodeEnfant(), "ENFANT_4_ENFANT");

        assertEquals(TypeTarifEnum.ENFANT, tarif.getType());
        assertEquals(4, tarif.getNbEnfant());
        assertEquals(Boolean.FALSE, tarif.getAdherent());
    }

    @Test
    public void testChaqueCombinaisonRecherchableAuCalculExisteUneSeuleFois() {
        // Le calcul recherche un tarif par (type, adherent, nbEnfant) et prend le premier
        // résultat : chaque combinaison doit exister exactement une fois.
        List<TarifEntity> tarifs = creerLesTarifsDUnePeriodeEnfant();

        Set<String> combinaisons = new HashSet<>();
        for (TarifEntity tarif : tarifs) {
            String combinaison = tarif.getType() + "|" + tarif.getAdherent() + "|" + tarif.getNbEnfant();
            assertTrue(combinaisons.add(combinaison),
                    "Deux tarifs partagent la combinaison " + combinaison + " (code " + tarif.getCode() + ")");
        }

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
    public void testUnePeriodeEnfantOuvreSeizeTarifs() {
        List<TarifEntity> tarifs = creerLesTarifsDUnePeriodeEnfant();

        assertEquals(16, tarifs.size());
        tarifs.forEach(tarif -> {
            assertNotNull(tarif.getCode());
            assertNotNull(tarif.getType());
            assertEquals(periodeCoursEnfant, tarif.getPeriode());
        });
    }

    @Test
    public void testLesTarifsEnfantSontRepartisEnHuitBaseEtHuitParEnfant() {
        List<TarifEntity> tarifs = creerLesTarifsDUnePeriodeEnfant();

        assertEquals(8, tarifs.stream().filter(tarif -> tarif.getType() == TypeTarifEnum.BASE).count());
        assertEquals(8, tarifs.stream().filter(tarif -> tarif.getType() == TypeTarifEnum.ENFANT).count());
    }

    @Test
    public void testLesMontantsSaisisSontAffectesAuBonCodeTarif() {
        List<TarifEntity> tarifs = creerLesTarifsDUnePeriodeEnfant();

        assertEquals(BigDecimal.valueOf(12), tarifDuCode(tarifs, "ENFANT_4_ENFANT").getMontant());
        assertEquals(BigDecimal.valueOf(165), tarifDuCode(tarifs, "BASE_4_ENFANT").getMontant());
        assertEquals(BigDecimal.valueOf(101), tarifDuCode(tarifs, "BASE_1_ENFANT").getMontant());
        assertEquals(BigDecimal.valueOf(104), tarifDuCode(tarifs, "ENFANT_ADHERENT_1_ENFANT").getMontant());
        // Aucun montant ne doit se retrouver sur deux codes différents
        assertEquals(16, tarifs.stream().map(TarifEntity::getMontant).distinct().count());
    }

    @Test
    public void testUnePeriodeDejaTarifeeNeRecreePasDeTarif() {
        // Sur une période existante, seuls les montants sont mis à jour :
        // le type des tarifs déjà en base n'est pas recalculé depuis les annotations.
        TarifEntity existant = TarifEntity.builder().code("ENFANT_4_ENFANT").type(TypeTarifEnum.ENFANT)
                .adherent(false).nbEnfant(4).periode(periodeCoursEnfant).montant(BigDecimal.TEN).build();
        when(periodeRepository.findById(ID_PERIODE)).thenReturn(Optional.of(periodeCoursEnfant));
        when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(List.of(existant));

        underTest.saveInfoTarif(grilleTarifaireComplete());

        assertEquals(1, capturerTarifsSauvegardes().size());
        assertEquals(BigDecimal.valueOf(12), existant.getMontant());
        assertEquals(TypeTarifEnum.ENFANT, existant.getType());
    }

    @Test
    public void testLaLectureDesTarifsRepositionneChaqueMontantSurSonChamp() {
        // findInfoTarifByPeriode retrouve le champ par son code : c'est le pendant lecture
        // de la création, et il doit rester cohérent avec les annotations.
        when(periodeRepository.findById(ID_PERIODE)).thenReturn(Optional.of(periodeCoursEnfant));
        when(tarifRepository.findByPeriodeId(ID_PERIODE)).thenReturn(List.of(
                TarifEntity.builder().code("ENFANT_4_ENFANT").type(TypeTarifEnum.ENFANT)
                        .adherent(false).nbEnfant(4).montant(BigDecimal.valueOf(12)).build(),
                TarifEntity.builder().code("BASE_4_ENFANT").type(TypeTarifEnum.BASE)
                        .adherent(false).nbEnfant(4).montant(BigDecimal.valueOf(165)).build()));

        InfoTarifDto result = underTest.findInfoTarifByPeriode(ID_PERIODE);

        assertEquals(BigDecimal.valueOf(12), result.getMontantEnfant4Enfant());
        assertEquals(BigDecimal.valueOf(165), result.getMontantBase4Enfant());
    }

    @Test
    public void testLaLectureEchoueSiLaPeriodeNexistePas() {
        when(periodeRepository.findById(404L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> underTest.findInfoTarifByPeriode(404L));

        assertTrue(exception.getMessage().contains("404"));
        verify(tarifRepository, never()).findByPeriodeId(anyLong());
    }
}
