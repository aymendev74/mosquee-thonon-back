package org.mosqueethonon.service.impl.adhesion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionPatchDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapper;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapperImpl;

@ExtendWith(MockitoExtension.class)
public class TestAdhesionServiceImpl {

    @Mock
    private AdhesionRepository adhesionRepository;

    @Spy
    private AdhesionMapper adhesionMapper = new AdhesionMapperImpl();

    @Mock
    private MailingConfirmationRepository mailingConfirmationRepository;

    @InjectMocks
    private AdhesionServiceImpl adhesionService;

    @Test
    public void testCreateAdhesion() {
        AdhesionDto adhesionDto = new AdhesionDto();

        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setId(1L);
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity.setDateInscription(LocalDateTime.now());

        when(adhesionRepository.save(any())).thenReturn(adhesionEntity);

        AdhesionDto result = adhesionService.createAdhesion(adhesionDto);

        assertNotNull(result);
        assertEquals(adhesionEntity.getStatut(), result.getStatut());
        verify(adhesionMapper).mapDtoToEntity(any(), any());
        verify(adhesionRepository).save(any());
        verify(mailingConfirmationRepository).save(any());
    }

    @Test
    public void testFindAdhesionById_WhenAdhesionExists() {
        // Arrange
        Long id = 1L;
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setId(id);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(new AdhesionDto());

        // Act
        AdhesionDto result = adhesionService.findAdhesionById(id);

        // Assert
        assertNotNull(result);
        verify(adhesionRepository).findById(id);
    }

    @Test
    public void testFindAdhesionById_WhenAdhesionDoesNotExist() {
        // Arrange
        Long id = 1L;

        when(adhesionRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        AdhesionDto result = adhesionService.findAdhesionById(id);

        // Assert
        assertNull(result);
        verify(adhesionRepository).findById(id);
    }

    @Test
    public void testDeleteAdhesions() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L);

        // Act
        Set<Long> result = adhesionService.deleteAdhesions(ids);

        // Assert
        assertNotNull(result);
        assertEquals(ids, result);
        verify(adhesionRepository).deleteAllById(ids);
    }

    @Test
    public void testPatchAdhesions_WhenAdhesionsExist() {
        // Arrange
        AdhesionPatchDto adhesionPatchDto = new AdhesionPatchDto();
        adhesionPatchDto.setIds(List.of(1L, 2L));
        adhesionPatchDto.setStatut(StatutInscription.VALIDEE);

        AdhesionEntity adhesion1 = new AdhesionEntity();
        adhesion1.setId(1L);
        adhesion1.setStatut(StatutInscription.PROVISOIRE);

        AdhesionEntity adhesion2 = new AdhesionEntity();
        adhesion2.setId(2L);
        adhesion2.setStatut(StatutInscription.PROVISOIRE);

        when(adhesionRepository.findById(1L)).thenReturn(Optional.of(adhesion1));
        when(adhesionRepository.findById(2L)).thenReturn(Optional.of(adhesion2));
        when(adhesionRepository.saveAll(any())).thenReturn(List.of(adhesion1, adhesion2));

        // Act
        Set<Long> result = adhesionService.patchAdhesions(adhesionPatchDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertEquals(StatutInscription.VALIDEE, adhesion1.getStatut());
        assertEquals(StatutInscription.VALIDEE, adhesion2.getStatut());
        verify(adhesionRepository).saveAll(any());
    }

    @Test
    public void testPatchAdhesions_WhenNoAdhesionsExist() {
        // Arrange
        AdhesionPatchDto adhesionPatchDto = new AdhesionPatchDto();
        adhesionPatchDto.setIds(List.of(1L, 2L));
        adhesionPatchDto.setStatut(StatutInscription.VALIDEE);

        when(adhesionRepository.findById(1L)).thenReturn(Optional.empty());
        when(adhesionRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Set<Long> result = adhesionService.patchAdhesions(adhesionPatchDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(adhesionRepository, never()).saveAll(any());
    }

    @Test
    public void testUpdateAdhesion_WhenAdhesionExists() {
        // Arrange
        Long id = 1L;
        AdhesionDto adhesionDto = new AdhesionDto();

        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setId(id);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionRepository.save(adhesionEntity)).thenReturn(adhesionEntity);
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(adhesionDto);

        // Act
        AdhesionDto result = adhesionService.updateAdhesion(id, adhesionDto);

        // Assert
        assertNotNull(result);
        verify(adhesionRepository).findById(id);
        verify(adhesionMapper).mapDtoToEntity(adhesionDto, adhesionEntity);
        verify(adhesionRepository).save(adhesionEntity);
    }

    @Test
    public void testUpdateAdhesion_WhenAdhesionDoesNotExist() {
        // Arrange
        Long id = 1L;
        AdhesionDto adhesionDto = new AdhesionDto();

        when(adhesionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adhesionService.updateAdhesion(id, adhesionDto);
        });
        assertEquals("Inscription not found ! idinsc = " + id, exception.getMessage());
        verify(adhesionRepository).findById(id);
        verify(adhesionMapper, never()).mapDtoToEntity(any(), any());
        verify(adhesionRepository, never()).save(any());
    }
}
