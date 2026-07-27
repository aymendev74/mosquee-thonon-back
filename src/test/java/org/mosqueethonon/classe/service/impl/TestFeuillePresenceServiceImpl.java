package org.mosqueethonon.classe.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.classe.entity.ClasseEntity;
import org.mosqueethonon.classe.entity.ClasseFeuillePresenceEntity;
import org.mosqueethonon.classe.entity.FeuillePresenceEntity;
import org.mosqueethonon.classe.repository.ClasseFeuillePresenceRepository;
import org.mosqueethonon.classe.repository.ClasseRepository;
import org.mosqueethonon.classe.repository.FeuillePresenceRepository;
import org.mosqueethonon.classe.v1.dto.FeuillePresenceDto;
import org.mosqueethonon.classe.v1.dto.PresenceEleveDto;
import org.mosqueethonon.classe.v1.mapper.FeuillePresenceMapper;
import org.mosqueethonon.common.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestFeuillePresenceServiceImpl {

    @Mock
    private ClasseFeuillePresenceRepository classeFeuillePresenceRepository;

    @Mock
    private ClasseRepository classeRepository;

    @Mock
    private FeuillePresenceRepository feuillePresenceRepository;

    @Mock
    private FeuillePresenceMapper feuillePresenceMapper;

    @InjectMocks
    private FeuillePresenceServiceImpl underTest;

    private PresenceEleveDto presence(Long idEleve, boolean present) {
        PresenceEleveDto dto = new PresenceEleveDto();
        dto.setIdEleve(idEleve);
        dto.setPresent(present);
        return dto;
    }

    private FeuillePresenceDto dto(LocalDate date, PresenceEleveDto... presences) {
        FeuillePresenceDto dto = new FeuillePresenceDto();
        dto.setDate(date);
        dto.setPresenceEleves(new ArrayList<>(List.of(presences)));
        return dto;
    }

    @Nested
    class QuandOnCreeUneFeuilleDePresence {

        @Test
        public void testEchoueSiLaClasseNexistePas() {
            // GIVEN
            when(classeRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> underTest.createFeuillePresence(404L, dto(LocalDate.now())));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
            verify(classeFeuillePresenceRepository, never()).save(any());
        }

        @Test
        public void testRattacheLaFeuilleALaClasseEtRecopieLesPresences() {
            // GIVEN
            when(classeRepository.findById(1L)).thenReturn(Optional.of(ClasseEntity.builder().id(1L).build()));
            when(classeFeuillePresenceRepository.save(any(ClasseFeuillePresenceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(feuillePresenceMapper.fromEntityToDto(any(ClasseFeuillePresenceEntity.class)))
                    .thenReturn(new FeuillePresenceDto());
            LocalDate date = LocalDate.of(2025, 10, 5);

            // WHEN
            FeuillePresenceDto result = underTest.createFeuillePresence(1L,
                    dto(date, presence(10L, true), presence(11L, false)));

            // THEN
            assertNotNull(result);
            ArgumentCaptor<ClasseFeuillePresenceEntity> captor =
                    ArgumentCaptor.forClass(ClasseFeuillePresenceEntity.class);
            verify(classeFeuillePresenceRepository).save(captor.capture());
            ClasseFeuillePresenceEntity saved = captor.getValue();
            assertEquals(1L, saved.getIdClasse());
            assertEquals(date, saved.getFeuillePresence().getDate());
            assertEquals(2, saved.getFeuillePresence().getElevesFeuillesPresences().size());
            assertTrue(saved.getFeuillePresence().getElevesFeuillesPresences().get(0).getPresent());
            assertEquals(11L, saved.getFeuillePresence().getElevesFeuillesPresences().get(1).getIdEleve());
        }
    }

    @Nested
    class QuandOnListeLesFeuillesDuneClasse {

        @Test
        public void testEchoueSiLaClasseNexistePas() {
            // GIVEN
            when(classeRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThrows(ResourceNotFoundException.class,
                    () -> underTest.findFeuillePresencesByClasseId(404L));
        }

        @Test
        public void testRetourneLesFeuillesTrieesParDate() {
            // GIVEN — les feuilles arrivent dans le désordre
            ClasseEntity classe = ClasseEntity.builder().id(1L)
                    .feuillesPresences(new ArrayList<>(List.of(
                            ClasseFeuillePresenceEntity.builder().id(1L).build(),
                            ClasseFeuillePresenceEntity.builder().id(2L).build(),
                            ClasseFeuillePresenceEntity.builder().id(3L).build())))
                    .build();
            when(classeRepository.findById(1L)).thenReturn(Optional.of(classe));
            when(feuillePresenceMapper.fromEntityToDto(any(ClasseFeuillePresenceEntity.class)))
                    .thenReturn(dto(LocalDate.of(2025, 12, 1)))
                    .thenReturn(dto(LocalDate.of(2025, 10, 1)))
                    .thenReturn(dto(LocalDate.of(2025, 11, 1)));

            // WHEN
            List<FeuillePresenceDto> result = underTest.findFeuillePresencesByClasseId(1L);

            // THEN
            assertEquals(List.of(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 11, 1),
                            LocalDate.of(2025, 12, 1)),
                    result.stream().map(FeuillePresenceDto::getDate).toList());
        }
    }

    @Nested
    class QuandOnModifieUneFeuilleDePresence {

        @Test
        public void testEchoueSiLaFeuilleNexistePas() {
            // GIVEN
            when(feuillePresenceRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> underTest.updateFeuillePresence(404L, new FeuillePresenceDto()));

            // THEN
            assertTrue(exception.getMessage().contains("404"));
            verify(feuillePresenceRepository, never()).save(any());
        }

        @Test
        public void testAppliqueLeDtoSurLEntiteExistantePuisSauvegarde() {
            // GIVEN
            FeuillePresenceEntity entity = FeuillePresenceEntity.builder().id(1L).build();
            FeuillePresenceDto dto = dto(LocalDate.of(2025, 10, 5));
            when(feuillePresenceRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(feuillePresenceMapper.fromEntityToDto(entity)).thenReturn(dto);

            // WHEN
            FeuillePresenceDto result = underTest.updateFeuillePresence(1L, dto);

            // THEN
            verify(feuillePresenceMapper).updateFeuillePresence(dto, entity);
            verify(feuillePresenceRepository).save(entity);
            assertEquals(dto, result);
        }
    }

    @Nested
    class QuandOnSupprimeUneFeuilleDePresence {

        @Test
        public void testEchoueSiLaFeuilleNexistePas() {
            // GIVEN
            when(feuillePresenceRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThrows(ResourceNotFoundException.class, () -> underTest.deleteFeuillePresence(404L));
            verify(classeFeuillePresenceRepository, never()).delete(any());
        }

        @Test
        public void testSupprimeDabordLeLienAvecLaClasse() {
            // GIVEN
            FeuillePresenceEntity feuille = FeuillePresenceEntity.builder().id(1L).build();
            ClasseFeuillePresenceEntity lien = ClasseFeuillePresenceEntity.builder().id(9L).build();
            when(feuillePresenceRepository.findById(1L)).thenReturn(Optional.of(feuille));
            when(classeFeuillePresenceRepository.findByFeuillePresenceId(1L)).thenReturn(lien);

            // WHEN
            underTest.deleteFeuillePresence(1L);

            // THEN — l'ordre compte : la contrainte de clé étrangère interdit l'inverse
            var inOrder = org.mockito.Mockito.inOrder(classeFeuillePresenceRepository, feuillePresenceRepository);
            inOrder.verify(classeFeuillePresenceRepository).delete(lien);
            inOrder.verify(feuillePresenceRepository).delete(feuille);
        }
    }
}
