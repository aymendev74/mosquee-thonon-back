package org.mosqueethonon.classe.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.classe.entity.ClasseActiviteEntity;
import org.mosqueethonon.classe.entity.ClasseEntity;
import org.mosqueethonon.classe.entity.LienClasseEleveEntity;
import org.mosqueethonon.classe.entity.LienClasseEnseignantEntity;
import org.mosqueethonon.classe.enums.JourActiviteEnum;
import org.mosqueethonon.classe.repository.ClasseRepository;
import org.mosqueethonon.classe.v1.criteria.CreateClasseCriteria;
import org.mosqueethonon.classe.v1.criteria.SearchClasseCriteria;
import org.mosqueethonon.classe.v1.dto.ClasseDto;
import org.mosqueethonon.classe.v1.mapper.ClasseMapper;
import org.mosqueethonon.common.exception.ForbiddenResourceAccessException;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.common.security.context.SecurityContext;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.inscription.repository.EleveRepository;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestClasseServiceImpl {

    private static final int ANNEE_DEBUT = 2025;
    private static final int ANNEE_FIN = 2026;

    @Mock
    private ClasseRepository classeRepository;

    @Mock
    private EleveRepository eleveRepository;

    @Mock
    private ClasseMapper classeMapper;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ClasseServiceImpl underTest;

    private EleveEntity eleve(Long id, String nom, NiveauInterneEnum niveau) {
        EleveEntity eleve = new EleveEntity();
        eleve.setId(id);
        eleve.setNom(nom);
        eleve.setPrenom("Prenom" + id);
        eleve.setDateNaissance(LocalDate.of(2015, 1, 1));
        eleve.setNiveauInterne(niveau);
        return eleve;
    }

    private ClasseEntity classeAvecEleves(Long id, JourActiviteEnum jour, EleveEntity... eleves) {
        List<LienClasseEleveEntity> liens = new ArrayList<>();
        for (EleveEntity eleve : eleves) {
            liens.add(LienClasseEleveEntity.builder().eleve(eleve).build());
        }
        return ClasseEntity.builder().id(id)
                .debutAnneeScolaire(ANNEE_DEBUT).finAnneeScolaire(ANNEE_FIN)
                .liensClasseEleves(liens)
                .activites(new ArrayList<>(List.of(ClasseActiviteEntity.builder().jour(jour).build())))
                .build();
    }

    private CreateClasseCriteria criteresCreation(int nbMaxEleveParClasse) {
        CreateClasseCriteria criteria = new CreateClasseCriteria();
        criteria.setDebutAnneeScolaire(ANNEE_DEBUT);
        criteria.setFinAnneeScolaire(ANNEE_FIN);
        criteria.setNbMaxEleveParClasse(nbMaxEleveParClasse);
        return criteria;
    }

    @SuppressWarnings("unchecked")
    private List<ClasseEntity> capturerClassesCreees() {
        ArgumentCaptor<List<ClasseEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(classeRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Nested
    class QuandOnGenereLesClassesDeLAnnee {

        @Test
        public void testNeFaitRienSAucunEleveNestInscrit() {
            // GIVEN
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(Collections.emptyList());

            // WHEN
            underTest.createClasses(criteresCreation(10));

            // THEN
            verify(classeRepository, never()).saveAll(anyList());
            verify(classeRepository, never()).findByDebutAnneeScolaireAndFinAnneeScolaire(
                    Mockito.anyInt(), Mockito.anyInt());
        }

        @Test
        public void testGroupeLesEleveParNiveau() {
            // GIVEN — 3 élèves, 2 niveaux différents, aucun antécédent
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(List.of(eleve(1L, "A", NiveauInterneEnum.P1),
                            eleve(2L, "B", NiveauInterneEnum.P1),
                            eleve(3L, "C", NiveauInterneEnum.P2)));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(
                    ANNEE_DEBUT - 1, ANNEE_FIN - 1)).thenReturn(Collections.emptyList());

            // WHEN
            underTest.createClasses(criteresCreation(10));

            // THEN — une classe par niveau
            List<ClasseEntity> classes = capturerClassesCreees();
            assertEquals(2, classes.size());
            assertEquals(2, classes.stream().filter(c -> c.getNiveau() == NiveauInterneEnum.P1)
                    .findFirst().orElseThrow().getLiensClasseEleves().size());
        }

        @Test
        public void testDecoupeUnNiveauEnPlusieursClassesAuDelaDuMaximum() {
            // GIVEN — 5 élèves d'un même niveau, 2 par classe
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(List.of(eleve(1L, "A", NiveauInterneEnum.P1), eleve(2L, "B", NiveauInterneEnum.P1),
                            eleve(3L, "C", NiveauInterneEnum.P1), eleve(4L, "D", NiveauInterneEnum.P1),
                            eleve(5L, "E", NiveauInterneEnum.P1)));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(
                    ANNEE_DEBUT - 1, ANNEE_FIN - 1)).thenReturn(Collections.emptyList());

            // WHEN
            underTest.createClasses(criteresCreation(2));

            // THEN — 3 classes (2+2+1), toutes libellées puisqu'il y en a plusieurs
            List<ClasseEntity> classes = capturerClassesCreees();
            assertEquals(3, classes.size());
            assertEquals(List.of("1", "2", "3"), classes.stream().map(ClasseEntity::getLibelle).toList());
            assertEquals(1, classes.get(2).getLiensClasseEleves().size());
        }

        @Test
        public void testNeLibellePasUneClasseUniqueDeSonNiveau() {
            // GIVEN
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(List.of(eleve(1L, "A", NiveauInterneEnum.P1)));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(
                    ANNEE_DEBUT - 1, ANNEE_FIN - 1)).thenReturn(Collections.emptyList());

            // WHEN
            underTest.createClasses(criteresCreation(10));

            // THEN
            assertNull(capturerClassesCreees().get(0).getLibelle());
        }

        @Test
        public void testReconduitLeJourDActiviteDeLAnneePrecedente() {
            // GIVEN — l'élève était en classe du dimanche matin l'an dernier
            EleveEntity ancien = eleve(9L, "Dupont", NiveauInterneEnum.P1);
            EleveEntity memeEleveCetteAnnee = eleve(1L, "Dupont", NiveauInterneEnum.P1);
            memeEleveCetteAnnee.setPrenom(ancien.getPrenom());
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(List.of(memeEleveCetteAnnee));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT - 1, ANNEE_FIN - 1))
                    .thenReturn(List.of(classeAvecEleves(77L, JourActiviteEnum.DIMANCHE_MATIN, ancien)));

            // WHEN
            underTest.createClasses(criteresCreation(10));

            // THEN
            List<ClasseEntity> classes = capturerClassesCreees();
            assertEquals(1, classes.size());
            assertEquals(JourActiviteEnum.DIMANCHE_MATIN, classes.get(0).getActivites().get(0).getJour());
        }

        @Test
        public void testPlaceLesNouveauxEleveDansLeGroupeSansJour() {
            // GIVEN — aucun antécédent : le jour d'activité reste indéterminé
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(List.of(eleve(1L, "Nouveau", NiveauInterneEnum.P1)));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT - 1, ANNEE_FIN - 1))
                    .thenReturn(Collections.emptyList());

            // WHEN
            underTest.createClasses(criteresCreation(10));

            // THEN
            assertNull(capturerClassesCreees().get(0).getActivites().get(0).getJour());
        }

        @Test
        public void testIgnoreUneClasseDeLAnneePrecedenteSansActivite() {
            // GIVEN — classe de l'an dernier sans activité renseignée : on ne peut rien en déduire
            EleveEntity ancien = eleve(9L, "Dupont", NiveauInterneEnum.P1);
            ClasseEntity classeSansActivite = classeAvecEleves(77L, null, ancien);
            classeSansActivite.setActivites(Collections.emptyList());
            EleveEntity memeEleve = eleve(1L, "Dupont", NiveauInterneEnum.P1);
            memeEleve.setPrenom(ancien.getPrenom());
            when(eleveRepository.findElevesEnfantByAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN, true))
                    .thenReturn(List.of(memeEleve));
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT - 1, ANNEE_FIN - 1))
                    .thenReturn(List.of(classeSansActivite));

            // WHEN
            underTest.createClasses(criteresCreation(10));

            // THEN
            assertNull(capturerClassesCreees().get(0).getActivites().get(0).getJour());
        }
    }

    @Nested
    class QuandOnCreeUneClasse {

        @Test
        public void testAttribueLeNiveauDeLaClasseAuxElevesQuiNenOntPas() {
            // GIVEN
            EleveEntity sansNiveau = eleve(1L, "A", null);
            ClasseEntity entity = classeAvecEleves(5L, JourActiviteEnum.SAMEDI_MATIN, sansNiveau);
            entity.setNiveau(NiveauInterneEnum.N1_1);
            ClasseDto dto = new ClasseDto();
            when(classeMapper.fromDtoToEntity(dto)).thenReturn(entity);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(sansNiveau));
            when(classeRepository.save(entity)).thenReturn(entity);
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN))
                    .thenReturn(List.of(entity));

            // WHEN
            underTest.createClasse(dto);

            // THEN
            assertEquals(NiveauInterneEnum.N1_1, sansNiveau.getNiveauInterne());
            verify(eleveRepository).save(sansNiveau);
        }

        @Test
        public void testNecrasePasLeNiveauDejaRenseigneDUnEleve() {
            // GIVEN
            EleveEntity avecNiveau = eleve(1L, "A", NiveauInterneEnum.P2);
            ClasseEntity entity = classeAvecEleves(5L, JourActiviteEnum.SAMEDI_MATIN, avecNiveau);
            entity.setNiveau(NiveauInterneEnum.N1_1);
            ClasseDto dto = new ClasseDto();
            when(classeMapper.fromDtoToEntity(dto)).thenReturn(entity);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(avecNiveau));
            when(classeRepository.save(entity)).thenReturn(entity);
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN))
                    .thenReturn(List.of(entity));

            // WHEN
            underTest.createClasse(dto);

            // THEN
            assertEquals(NiveauInterneEnum.P2, avecNiveau.getNiveauInterne());
            verify(eleveRepository, never()).save(any(EleveEntity.class));
        }

        @Test
        public void testEchoueSiUnEleveDeLEffectifNexistePas() {
            // GIVEN
            ClasseEntity entity = classeAvecEleves(5L, JourActiviteEnum.SAMEDI_MATIN, eleve(404L, "Fantome", null));
            ClasseDto dto = new ClasseDto();
            when(classeMapper.fromDtoToEntity(dto)).thenReturn(entity);
            when(eleveRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> underTest.createClasse(dto));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
            verify(classeRepository, never()).save(any());
        }

        @Test
        public void testRetireLesElevesDesAutresClassesDeLaMemePeriode() {
            // GIVEN — l'élève 1 était dans une autre classe de la même année
            EleveEntity eleve = eleve(1L, "A", NiveauInterneEnum.P1);
            ClasseEntity nouvelle = classeAvecEleves(5L, JourActiviteEnum.SAMEDI_MATIN, eleve);
            ClasseEntity autre = classeAvecEleves(6L, JourActiviteEnum.DIMANCHE_MATIN, eleve);
            ClasseDto dto = new ClasseDto();
            when(classeMapper.fromDtoToEntity(dto)).thenReturn(nouvelle);
            when(eleveRepository.findById(1L)).thenReturn(Optional.of(eleve));
            when(classeRepository.save(nouvelle)).thenReturn(nouvelle);
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN))
                    .thenReturn(List.of(nouvelle, autre));

            // WHEN
            underTest.createClasse(dto);

            // THEN — un élève n'appartient qu'à une seule classe par période
            assertTrue(autre.getLiensClasseEleves().isEmpty());
            verify(classeRepository).save(autre);
        }
    }

    @Nested
    class QuandOnModifieUneClasse {

        @Test
        public void testEchoueSiLaClasseNexistePas() {
            // GIVEN
            when(classeRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> underTest.updateClasse(404L, new ClasseDto()));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
        }
    }

    @Nested
    class QuandOnRechercheDesClasses {

        @Test
        public void testUnAdminVoitToutesLesClassesDeLaPeriode() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(true);
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(ANNEE_DEBUT, ANNEE_FIN))
                    .thenReturn(List.of(classeAvecEleves(1L, JourActiviteEnum.SAMEDI_MATIN)));
            when(classeMapper.fromEntityToDto(any(ClasseEntity.class))).thenReturn(new ClasseDto());
            SearchClasseCriteria criteria = new SearchClasseCriteria();
            criteria.setAnneeDebut(ANNEE_DEBUT);
            criteria.setAnneeFin(ANNEE_FIN);

            // WHEN
            List<ClasseDto> result = underTest.findClassesByCriteria(criteria);

            // THEN
            assertEquals(1, result.size());
            verify(classeRepository, never()).findByDebutAnneeScolaireAndFinAnneeScolaireAndEnseignantUsername(
                    Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
        }

        @Test
        public void testUnEnseignantNeVoitQueSesClasses() {
            // GIVEN
            when(securityContext.isAdmin()).thenReturn(false);
            when(securityContext.getVisa()).thenReturn("prof");
            when(classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaireAndEnseignantUsername(
                    ANNEE_DEBUT, ANNEE_FIN, "prof")).thenReturn(Collections.emptyList());
            SearchClasseCriteria criteria = new SearchClasseCriteria();
            criteria.setAnneeDebut(ANNEE_DEBUT);
            criteria.setAnneeFin(ANNEE_FIN);

            // WHEN
            List<ClasseDto> result = underTest.findClassesByCriteria(criteria);

            // THEN
            assertTrue(result.isEmpty());
            verify(classeRepository, never()).findByDebutAnneeScolaireAndFinAnneeScolaire(
                    Mockito.anyInt(), Mockito.anyInt());
        }
    }

    @Nested
    class QuandOnLitUneClasseParSonIdentifiant {

        private ClasseEntity classeDuProf(String username) {
            UtilisateurEntity enseignant = new UtilisateurEntity();
            enseignant.setUsername(username);
            ClasseEntity classe = classeAvecEleves(1L, JourActiviteEnum.SAMEDI_MATIN);
            classe.setLiensClasseEnseignants(new ArrayList<>(List.of(
                    LienClasseEnseignantEntity.builder().enseignant(enseignant).build())));
            return classe;
        }

        @Test
        public void testEchoueSiLaClasseNexistePas() {
            // GIVEN
            when(classeRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThrows(ResourceNotFoundException.class, () -> underTest.findClasseById(404L));
        }

        @Test
        public void testUnAdminAccedeAToutesLesClasses() {
            // GIVEN
            when(classeRepository.findById(1L)).thenReturn(Optional.of(classeDuProf("quelquun")));
            when(securityContext.isAdmin()).thenReturn(true);
            when(classeMapper.fromEntityToDto(any(ClasseEntity.class))).thenReturn(new ClasseDto());

            // WHEN
            ClasseDto result = underTest.findClasseById(1L);

            // THEN
            assertNotNull(result);
        }

        @Test
        public void testUnEnseignantAccedeALaClasseDontIlEstTitulaire() {
            // GIVEN
            when(classeRepository.findById(1L)).thenReturn(Optional.of(classeDuProf("prof")));
            when(securityContext.isAdmin()).thenReturn(false);
            when(securityContext.getVisa()).thenReturn("prof");
            when(classeMapper.fromEntityToDto(any(ClasseEntity.class))).thenReturn(new ClasseDto());

            // WHEN
            ClasseDto result = underTest.findClasseById(1L);

            // THEN
            assertNotNull(result);
        }

        @Test
        public void testUnEnseignantNAccedePasALaClasseDunAutre() {
            // GIVEN
            when(classeRepository.findById(1L)).thenReturn(Optional.of(classeDuProf("autre-prof")));
            when(securityContext.isAdmin()).thenReturn(false);
            when(securityContext.getVisa()).thenReturn("prof");

            // WHEN
            ForbiddenResourceAccessException exception = assertThrows(ForbiddenResourceAccessException.class,
                    () -> underTest.findClasseById(1L));

            // THEN
            assertTrue(exception.getMessage().contains("prof"));
            verify(classeMapper, never()).fromEntityToDto(any(ClasseEntity.class));
        }
    }

    @Nested
    class QuandOnSupprimeUneClasse {

        @Test
        public void testDelegueLaSuppressionAuRepository() {
            // WHEN
            underTest.deleteClasse(3L);

            // THEN
            verify(classeRepository).deleteById(3L);
        }
    }
}
