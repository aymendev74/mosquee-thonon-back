package org.mosqueethonon.service.impl.adhesion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.mosqueethonon.service.document.DocumentService;
import org.mosqueethonon.service.lock.LockService;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionSaveCriteria;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapper;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapperImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestAdhesionServiceImpl {

    @Mock
    private AdhesionRepository adhesionRepository;

    @Spy
    private AdhesionMapper adhesionMapper = new AdhesionMapperImpl();

    @Mock
    private MailRequestRepository mailRequestRepository;

    @Mock
    private LockService lockService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private AsyncDocumentService asyncDocumentService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private AdhesionServiceImpl adhesionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        // Stub par défaut : aucun document trouvé (évite NPE dans les tests existants)
        lenient().when(documentRepository.findByMetadataKeyAndValue(any(), anyString()))
                .thenReturn(Optional.empty());
    }

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
        verify(adhesionMapper).updateAdhesion(any(), any());
        verify(adhesionRepository).save(any());
        verify(mailRequestRepository).save(any());
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
        verify(adhesionRepository, times(2)).deleteById(any());
        verify(this.mailRequestRepository, times(2)).deleteByTypeAndBusinessIdIn(eq(MailRequestType.ADHESION), any());
        verify(this.documentRequestRepository, times(2)).deleteByTypeAndBusinessIdIn(eq(DocumentRequestType.ADHESION), any());
        verify(this.documentService, never()).deleteDocument(any());
    }

    @Test
    public void testDeleteAdhesions_SupprimeLeDocumentAssocie_QuandDocumentTrouve() {
        // Arrange
        Long id = 1L;
        DocumentEntity doc = new DocumentEntity();
        doc.setId(55L);

        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_ADHESION), eq(String.valueOf(id))))
                .thenReturn(Optional.of(doc));

        // Act
        Set<Long> result = adhesionService.deleteAdhesions(Set.of(id));

        // Assert
        assertEquals(Set.of(id), result);
        verify(documentRequestRepository).deleteByTypeAndBusinessIdIn(eq(DocumentRequestType.ADHESION), eq(Set.of(id)));
        verify(documentService).deleteDocument(55L);
        verify(adhesionRepository).deleteById(id);
    }

    @Test
    public void testDeleteAdhesions_NeSupprimeAucunDocument_QuandAucunDocumentTrouve() {
        // Arrange
        Long id = 1L;
        // documentRepository retourne empty (stub par défaut du @BeforeEach)

        // Act
        Set<Long> result = adhesionService.deleteAdhesions(Set.of(id));

        // Assert
        assertEquals(Set.of(id), result);
        verify(documentRequestRepository).deleteByTypeAndBusinessIdIn(eq(DocumentRequestType.ADHESION), eq(Set.of(id)));
        verify(documentService, never()).deleteDocument(any());
        verify(adhesionRepository).deleteById(id);
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
        verify(mailRequestRepository, times(2)).save(any());
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
        var adhesionCriteria = AdhesionSaveCriteria.builder().build();
        Long id = 1L;
        AdhesionDto adhesionDto = new AdhesionDto();
        adhesionDto.setStatut(StatutInscription.VALIDEE);

        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity.setId(id);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionRepository.save(adhesionEntity)).thenReturn(adhesionEntity);
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(adhesionDto);

        // Act
        AdhesionDto result = adhesionService.updateAdhesion(id, adhesionDto, adhesionCriteria);

        // Assert
        assertNotNull(result);
        verify(adhesionRepository).findById(id);
        verify(adhesionMapper).updateAdhesion(adhesionDto, adhesionEntity);
        verify(adhesionRepository).save(adhesionEntity);
        verify(mailRequestRepository).save(any());
    }

    @Test
    public void testUpdateAdhesion_WhenAdhesionDoesNotExist() {
        // Arrange
        var adhesionCriteria = AdhesionSaveCriteria.builder().build();
        Long id = 1L;
        AdhesionDto adhesionDto = new AdhesionDto();

        when(adhesionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adhesionService.updateAdhesion(id, adhesionDto, adhesionCriteria);
        });
        assertEquals("Inscription not found ! idinsc = " + id, exception.getMessage());
        verify(adhesionRepository).findById(id);
        verify(adhesionMapper, never()).updateAdhesion(any(), any());
        verify(adhesionRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument peuplé
    // ---------------------------------------------------------------------------

    @Test
    public void testCreateAdhesion_IdDocumentNonPeuple_CarAsynchrone() {
        // createAdhesion appelle requestDocumentGeneration de façon asynchrone ;
        // le document n'existe pas encore au moment du retour → idDocument reste null.
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setId(1L);
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity.setDateInscription(LocalDateTime.now());

        when(adhesionRepository.save(any())).thenReturn(adhesionEntity);
        // documentRepository retourne empty (stub par défaut du @BeforeEach)

        AdhesionDto result = adhesionService.createAdhesion(new AdhesionDto());

        assertNotNull(result);
        assertNull(result.getIdDocument());
        verify(asyncDocumentService, times(1))
                .requestDocumentGeneration(eq(DocumentRequestType.ADHESION), eq(1L));
    }

    @Test
    public void testFindAdhesionById_IdDocumentPeuple_QuandDocumentTrouve() {
        // Arrange
        Long id = 1L;
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setId(id);
        AdhesionDto adhesionDto = new AdhesionDto();
        adhesionDto.setId(id);

        DocumentEntity doc = new DocumentEntity();
        doc.setId(33L);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(adhesionDto);
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_ADHESION), eq(String.valueOf(id))))
                .thenReturn(Optional.of(doc));

        // Act
        AdhesionDto result = adhesionService.findAdhesionById(id);

        // Assert
        assertNotNull(result);
        assertEquals(33L, result.getIdDocument());
    }

    @Test
    public void testFindAdhesionById_IdDocumentNull_QuandAucunDocument() {
        // Arrange
        Long id = 1L;
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setId(id);
        AdhesionDto adhesionDto = new AdhesionDto();
        adhesionDto.setId(id);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(adhesionDto);
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_ADHESION), eq(String.valueOf(id))))
                .thenReturn(Optional.empty());

        // Act
        AdhesionDto result = adhesionService.findAdhesionById(id);

        // Assert
        assertNotNull(result);
        assertNull(result.getIdDocument());
    }

    @Test
    public void testUpdateAdhesion_IdDocumentPeuple_QuandDocumentTrouve() {
        // Arrange
        Long id = 1L;
        AdhesionDto adhesionDto = new AdhesionDto();
        adhesionDto.setStatut(StatutInscription.VALIDEE);
        adhesionDto.setId(id);

        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity.setId(id);

        DocumentEntity doc = new DocumentEntity();
        doc.setId(44L);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionRepository.save(adhesionEntity)).thenReturn(adhesionEntity);
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(adhesionDto);
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_ADHESION), eq(String.valueOf(id))))
                .thenReturn(Optional.of(doc));

        // Act
        AdhesionDto result = adhesionService.updateAdhesion(id, adhesionDto, AdhesionSaveCriteria.builder().build());

        // Assert
        assertNotNull(result);
        assertEquals(44L, result.getIdDocument());
    }

    @Test
    public void testUpdateAdhesion_IdDocumentNull_QuandAucunDocument() {
        // Arrange
        Long id = 1L;
        AdhesionDto adhesionDto = new AdhesionDto();
        adhesionDto.setStatut(StatutInscription.VALIDEE);
        adhesionDto.setId(id);

        AdhesionEntity adhesionEntity = new AdhesionEntity();
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity.setId(id);

        when(adhesionRepository.findById(id)).thenReturn(Optional.of(adhesionEntity));
        when(adhesionRepository.save(adhesionEntity)).thenReturn(adhesionEntity);
        when(adhesionMapper.fromEntityToDto(adhesionEntity)).thenReturn(adhesionDto);
        // documentRepository retourne empty (stub par défaut du @BeforeEach)

        // Act
        AdhesionDto result = adhesionService.updateAdhesion(id, adhesionDto, AdhesionSaveCriteria.builder().build());

        // Assert
        assertNotNull(result);
        assertNull(result.getIdDocument());
    }
}
