package org.mosqueethonon.inscription.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.classe.entity.ClasseEntity;
import org.mosqueethonon.classe.entity.LienClasseEleveEntity;
import org.mosqueethonon.classe.repository.ClasseRepository;
import org.mosqueethonon.common.exception.BadRequestException;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.inscription.entity.EleveEnrichedEntity;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.inscription.enums.AffectationEleveEnum;
import org.mosqueethonon.inscription.enums.ResultatEnum;
import org.mosqueethonon.inscription.repository.EleveEnrichedRepository;
import org.mosqueethonon.inscription.repository.EleveRepository;
import org.mosqueethonon.inscription.v1.criteria.SearchEleveCriteria;
import org.mosqueethonon.inscription.v1.dto.EleveDto;
import org.mosqueethonon.inscription.v1.dto.EleveEnrichedDto;
import org.mosqueethonon.inscription.v1.mapper.EleveEnrichedMapper;
import org.mosqueethonon.inscription.v1.mapper.EleveMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestEleveServiceImpl {

    private static final int ANNEE_DEBUT = 2025;
    private static final int ANNEE_FIN = 2026;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private ClasseRepository classeRepository;

    @Mock
    private EleveRepository eleveRepository;

    @Mock
    private EleveMapper eleveMapper;

    @Mock
    private EleveEnrichedMapper eleveEnrichedMapper;

    @Mock
    private EleveEnrichedRepository eleveEnrichedRepository;

    @InjectMocks
    private EleveServiceImpl underTest;

    private EleveEntity eleve(Long id) {
        EleveEntity eleve = new EleveEntity();
        eleve.setId(id);
        return eleve;
    }

    private SearchEleveCriteria criteres(AffectationEleveEnum affectation) {
        SearchEleveCriteria criteria = new SearchEleveCriteria();
        criteria.setAnneeDebut(ANNEE_DEBUT);
        criteria.setAnneeFin(ANNEE_FIN);
        criteria.setAffectation(affectation);
        return criteria;
    }

    private ClasseEntity classeAvec(EleveEntity... eleves) {
        List<LienClasseEleveEntity> liens = new ArrayList<>();
        for (EleveEntity eleve : eleves) {
            liens.add(LienClasseEleveEntity.builder().eleve(eleve).build());
        }
        return ClasseEntity.builder().liensClasseEleves(liens).build();
    }

    private JsonNode json(String contenu) {
        try {
            return MAPPER.readTree(contenu);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Nested
    class QuandOnRechercheDesEleves {

        @Test
        public void testRetourneUneListeVideSansEleveSurLaPeriode() {
            // GIVEN
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, false))
                    .thenReturn(Collections.emptyList());

            // WHEN
            List<EleveDto> result = underTest.findElevesByCriteria(criteres(null));

            // THEN
            assertTrue(result.isEmpty());
            Mockito.verifyNoInteractions(classeRepository);
        }

        @Test
        public void testRetourneTousLesElevesQuandLAffectationNestPasUnCritere() {
            // GIVEN
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, false))
                    .thenReturn(List.of(eleve(1L), eleve(2L)));
            when(eleveMapper.fromEntityToDto(any(EleveEntity.class))).thenReturn(new EleveDto());

            // WHEN
            List<EleveDto> result = underTest.findElevesByCriteria(criteres(null));

            // THEN — pas besoin d'aller chercher les classes
            assertEquals(2, result.size());
            Mockito.verifyNoInteractions(classeRepository);
        }

        @Test
        public void testTraiteSansImportanceCommeAbsenceDeCritere() {
            // GIVEN
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, false))
                    .thenReturn(List.of(eleve(1L)));
            when(eleveMapper.fromEntityToDto(any(EleveEntity.class))).thenReturn(new EleveDto());

            // WHEN
            List<EleveDto> result = underTest.findElevesByCriteria(
                    criteres(AffectationEleveEnum.SANS_IMPORTANCE));

            // THEN
            assertEquals(1, result.size());
            Mockito.verifyNoInteractions(classeRepository);
        }

        @Test
        public void testNeGardeQueLesElevesAffectesAUneClasse() {
            // GIVEN — l'élève 1 est en classe, le 2 non
            EleveEntity affecte = eleve(1L);
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, false))
                    .thenReturn(List.of(affecte, eleve(2L)));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN))
                    .thenReturn(List.of(classeAvec(affecte)));
            when(eleveMapper.fromEntityToDto(any(EleveEntity.class))).thenReturn(new EleveDto());

            // WHEN
            List<EleveDto> result = underTest.findElevesByCriteria(
                    criteres(AffectationEleveEnum.AVEC_AFFECTATION));

            // THEN
            assertEquals(1, result.size());
        }

        @Test
        public void testNeGardeQueLesElevesSansClasse() {
            // GIVEN
            EleveEntity affecte = eleve(1L);
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, false))
                    .thenReturn(List.of(affecte, eleve(2L), eleve(3L)));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN))
                    .thenReturn(List.of(classeAvec(affecte)));
            when(eleveMapper.fromEntityToDto(any(EleveEntity.class))).thenReturn(new EleveDto());

            // WHEN
            List<EleveDto> result = underTest.findElevesByCriteria(
                    criteres(AffectationEleveEnum.SANS_AFFECTATION));

            // THEN
            assertEquals(2, result.size());
        }

        @Test
        public void testRelaieLeCritereAvecNiveauAuRepository() {
            // GIVEN
            SearchEleveCriteria criteria = criteres(null);
            criteria.setAvecNiveau(true);
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(Collections.emptyList());

            // WHEN
            underTest.findElevesByCriteria(criteria);

            // THEN
            verify(eleveRepository).findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true);
        }
    }

    @Nested
    class QuandOnPatcheDesEleves {

        @Test
        public void testRefuseUnCorpsSansChampEleves() {
            // GIVEN
            JsonNode patch = json("{\"autre\": []}");

            // WHEN / THEN
            assertThrows(BadRequestException.class, () -> underTest.patchEleves(patch));
        }

        @Test
        public void testRefuseUneListeElevesVide() {
            // GIVEN
            JsonNode patch = json("{\"eleves\": []}");

            // WHEN / THEN
            assertThrows(BadRequestException.class, () -> underTest.patchEleves(patch));
        }

        @Test
        public void testRefuseUnEleveSansIdentifiant() {
            // GIVEN
            JsonNode patch = json("{\"eleves\": [{\"resultat\": \"ACQUIS\"}]}");

            // WHEN / THEN
            assertThrows(BadRequestException.class, () -> underTest.patchEleves(patch));
        }

        @Test
        public void testRefuseUnIdentifiantNonNumerique() {
            // GIVEN
            JsonNode patch = json("{\"eleves\": [{\"id\": \"abc\"}]}");

            // WHEN / THEN
            assertThrows(BadRequestException.class, () -> underTest.patchEleves(patch));
        }

        @Test
        public void testEchoueSiLEleveNexistePas() {
            // GIVEN
            when(eleveRepository.findById(404L)).thenReturn(Optional.empty());
            JsonNode patch = json("{\"eleves\": [{\"id\": 404}]}");

            // WHEN
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> underTest.patchEleves(patch));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
        }

        @Test
        public void testMetAJourLeResultat() {
            // GIVEN
            EleveEntity eleve = eleve(1L);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(eleve));
            JsonNode patch = json("{\"eleves\": [{\"id\": 1, \"resultat\": \"ACQUIS\"}]}");

            // WHEN
            underTest.patchEleves(patch);

            // THEN
            assertEquals(ResultatEnum.ACQUIS, eleve.getResultat());
            verify(eleveRepository).save(eleve);
        }

        @Test
        public void testEffaceLeResultatQuandIlEstNull() {
            // GIVEN
            EleveEntity eleve = eleve(1L);
            eleve.setResultat(ResultatEnum.ACQUIS);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(eleve));
            JsonNode patch = json("{\"eleves\": [{\"id\": 1, \"resultat\": null}]}");

            // WHEN
            underTest.patchEleves(patch);

            // THEN
            assertNull(eleve.getResultat());
            verify(eleveRepository).save(eleve);
        }

        @Test
        public void testSauvegardeSansModificationQuandAucunChampNestFourni() {
            // GIVEN
            EleveEntity eleve = eleve(1L);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(eleve));
            JsonNode patch = json("{\"eleves\": [{\"id\": 1}]}");

            // WHEN
            underTest.patchEleves(patch);

            // THEN
            assertNull(eleve.getResultat());
            verify(eleveRepository).save(eleve);
        }

        @Test
        public void testPatcheChaqueEleveDeLaListe() {
            // GIVEN
            EleveEntity premier = eleve(1L);
            EleveEntity second = eleve(2L);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(premier));
            when(eleveRepository.findById(2L)).thenReturn(Optional.of(second));
            JsonNode patch = json(
                    "{\"eleves\": [{\"id\": 1, \"resultat\": \"ACQUIS\"}, {\"id\": 2, \"resultat\": \"NON_ACQUIS\"}]}");

            // WHEN
            underTest.patchEleves(patch);

            // THEN
            assertEquals(ResultatEnum.ACQUIS, premier.getResultat());
            assertEquals(ResultatEnum.NON_ACQUIS, second.getResultat());
            verify(eleveRepository, Mockito.times(2)).save(any(EleveEntity.class));
        }
    }

    @Nested
    class QuandOnLitUnEleve {

        @Test
        public void testRetourneNullQuandLEleveNexistePas() {
            // GIVEN
            when(eleveRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            EleveDto result = underTest.findEleveById(404L);

            // THEN
            assertNull(result);
            verify(eleveMapper, never()).fromEntityToDto(any(EleveEntity.class));
        }

        @Test
        public void testRetourneLeDtoQuandLEleveExiste() {
            // GIVEN
            EleveEntity eleve = eleve(1L);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(eleve));
            when(eleveMapper.fromEntityToDto(eleve)).thenReturn(new EleveDto());

            // WHEN
            EleveDto result = underTest.findEleveById(1L);

            // THEN
            assertNotNull(result);
        }

        @Test
        public void testListeLesElevesEnrichisDuneClasse() {
            // GIVEN
            when(eleveEnrichedRepository.findByIdClasseOrderByNomAscPrenomAsc(3L))
                    .thenReturn(List.of(new EleveEnrichedEntity(), new EleveEnrichedEntity()));
            when(eleveEnrichedMapper.fromEntityToDto(any(EleveEnrichedEntity.class)))
                    .thenReturn(new EleveEnrichedDto());

            // WHEN
            List<EleveEnrichedDto> result = underTest.findElevesEnrichedByIdClasse(3L);

            // THEN
            assertEquals(2, result.size());
        }
    }
}
