package org.mosqueethonon.service.impl.adhesion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
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

    private ObjectMapper objectMapper = new ObjectMapper();


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
        assertThrows(ResourceNotFoundException.class, () -> adhesionService.findAdhesionById(id));
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
        ObjectNode adhesions = this.objectMapper.createObjectNode();
        ArrayNode adhesionsArray = adhesions.putArray("adhesions");
        ObjectNode adhesionNode1 = this.objectMapper.createObjectNode();
        adhesionNode1.put("id", 1L);
        adhesionNode1.put("statut", "VALIDEE");
        adhesionsArray.add(adhesionNode1);
        ObjectNode adhesionNode2 = this.objectMapper.createObjectNode();
        adhesionNode2.put("id", 2L);
        adhesionNode2.put("statut", "VALIDEE");
        adhesionsArray.add(adhesionNode2);

        AdhesionEntity adhesion1 = new AdhesionEntity();
        adhesion1.setId(1L);
        adhesion1.setStatut(StatutInscription.PROVISOIRE);

        AdhesionEntity adhesion2 = new AdhesionEntity();
        adhesion2.setId(2L);
        adhesion2.setStatut(StatutInscription.PROVISOIRE);

        when(adhesionRepository.findById(1L)).thenReturn(Optional.of(adhesion1));
        when(adhesionRepository.findById(2L)).thenReturn(Optional.of(adhesion2));

        // Act
        Set<Long> result = adhesionService.patchAdhesions(adhesions);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertEquals(StatutInscription.VALIDEE, adhesion1.getStatut());
        assertEquals(StatutInscription.VALIDEE, adhesion2.getStatut());
        verify(adhesionRepository, times(2)).save(any());
    }

    @Test
    public void testPatchAdhesions_WhenNoAdhesionsExist() {
        ObjectNode adhesions = this.objectMapper.createObjectNode();
        ArrayNode adhesionsArray = adhesions.putArray("adhesions");
        ObjectNode adhesionNode1 = this.objectMapper.createObjectNode();
        adhesionNode1.put("id", 1L);
        adhesionNode1.put("statut", "VALIDEE");
        adhesionsArray.add(adhesionNode1);

        when(adhesionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adhesionService.patchAdhesions(adhesions));
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
