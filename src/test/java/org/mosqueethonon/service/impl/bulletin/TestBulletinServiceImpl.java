package org.mosqueethonon.service.impl.bulletin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NoteMatiereEnum;
import org.mosqueethonon.enums.TypeMatiereEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.MatiereRepository;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.bulletin.BulletinMatiereDto;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMapper;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMatiereMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class TestBulletinServiceImpl {

    @Mock
    private BulletinRepository bulletinRepository;

    @Mock
    private BulletinMapper bulletinMapper;

    @Mock
    private EleveService eleveService;

    @Mock
    private MatiereService matiereService;

    @Mock
    private BulletinMatiereMapper bulletinMatiereMapper;

    @Mock
    private AsyncDocumentService asyncDocumentService;

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MatiereRepository matiereRepository;

    @InjectMocks
    private BulletinServiceImpl bulletinService;

    @Test
    public void testCreateBulletin_BulletinIncomplet_DocumentGenerationNotCalled() {
        // GIVEN — bulletin sans appréciation : isBulletinComplet retourne false
        BulletinDto bulletinDto = new BulletinDto();
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        BulletinDto savedDto = new BulletinDto();
        savedDto.setId(1L);
        // savedDto n'a pas d'appréciation → isBulletinComplet == false
        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        BulletinDto result = this.bulletinService.createBulletin(bulletinDto);

        // THEN
        assertNotNull(result);
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
        assertNull(result.getIdDocument());
    }

    @Test
    public void testUpdateBulletinExpectThrowsResourceNotFoundExceptionWhenResourceNotFound() {
        // GIVEN
        BulletinDto bulletinDto = new BulletinDto();
        when(this.bulletinRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // WHEN THEN
        assertThrows(ResourceNotFoundException.class, () -> this.bulletinService.updateBulletin(1L, bulletinDto));
    }

    @Test
    public void testUpdateBulletin_BulletinIncomplet_DocumentGenerationNotCalled() {
        // GIVEN — savedDto sans appréciation : isBulletinComplet == false
        BulletinDto bulletinDto = new BulletinDto();
        BulletinMatiereDto bulletinMatiereDto = BulletinMatiereDto.builder().code(MatiereEnum.TAFFSIR_CORAN).build();
        bulletinDto.setBulletinMatieres(Lists.newArrayList(bulletinMatiereDto));
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        bulletinEntity.setBulletinMatieres(new ArrayList<>());
        BulletinDto savedDto = new BulletinDto();
        savedDto.setId(1L);
        when(this.bulletinRepository.findById(eq(1L))).thenReturn(Optional.of(bulletinEntity));
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.matiereService.findByCode(eq(MatiereEnum.TAFFSIR_CORAN))).thenReturn(Optional.of(new MatiereEntity()));
        when(this.bulletinMatiereMapper.fromDtoToEntity(any())).thenReturn(new BulletinMatiereEntity());
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        BulletinDto result = this.bulletinService.updateBulletin(1L, bulletinDto);

        // THEN
        assertNotNull(result);
        assertEquals(1, bulletinEntity.getBulletinMatieres().size());
        verify(this.bulletinMapper, times(1)).updateBulletinEntity(any(), any());
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testUpdateBulletin_BulletinComplet_DocumentGenerationCalled() {
        // GIVEN — savedDto complet : isBulletinComplet == true
        BulletinDto bulletinDto = new BulletinDto();
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        bulletinEntity.setBulletinMatieres(new ArrayList<>());

        BulletinDto savedDto = bulletinComplet();
        savedDto.setId(1L);

        MatiereEntity matiereEnfant = new MatiereEntity(1L, MatiereEnum.TAFFSIR_CORAN, TypeMatiereEnum.ENFANT);

        when(this.bulletinRepository.findById(eq(1L))).thenReturn(Optional.of(bulletinEntity));
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.matiereRepository.findByType(TypeMatiereEnum.ENFANT)).thenReturn(List.of(matiereEnfant));
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        BulletinDto result = this.bulletinService.updateBulletin(1L, bulletinDto);

        // THEN
        assertNotNull(result);
        verify(this.asyncDocumentService, times(1)).requestDocumentGeneration(eq(DocumentRequestType.BULLETIN), eq(1L));
    }

    @Test
    public void testDeleteBulletin() {
        // WHEN
        this.bulletinService.deleteBulletin(1L);

        // THEN
        verify(this.documentRequestRepository, times(1))
                .deleteByTypeAndBusinessIdIn(eq(DocumentRequestType.BULLETIN), eq(Set.of(1L)));
        verify(this.bulletinRepository, times(1)).deleteById(eq(1L));
    }

    @Test
    public void testFindBulletinsByIdEleveThrowsResourceNotFoundExceptionWhenResourceNotFound() {
        // GIVEN
        when(this.eleveService.findEleveById(anyLong())).thenReturn(null);

        // WHEN THEN
        assertThrows(ResourceNotFoundException.class, () -> this.bulletinService.findBulletinsByIdEleve(1L));
    }

    @Test
    public void testFindBulletinsByIdEleve() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        BulletinEntity bulletinEntity2 = new BulletinEntity();
        when(this.eleveService.findEleveById(anyLong())).thenReturn(new EleveDto());
        when(this.bulletinRepository.findByIdEleve(anyLong())).thenReturn(List.of(bulletinEntity, bulletinEntity2));
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(new BulletinDto());
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        List<BulletinDto> bulletinDtos = this.bulletinService.findBulletinsByIdEleve(1L);

        // THEN
        assertNotNull(bulletinDtos);
        assertEquals(2, bulletinDtos.size());
        verify(this.bulletinMapper, times(2)).fromEntityToDto(any());
    }

    // ---------------------------------------------------------------------------
    // Tests isBulletinComplet — comportement observable via createBulletin
    // ---------------------------------------------------------------------------

    @Test
    public void testCreateBulletin_BulletinComplet_DocumentGenerationCalled() {
        // GIVEN — bulletin avec tous les champs requis et une matière ENFANT avec note
        BulletinDto bulletinDto = new BulletinDto();
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);

        BulletinDto savedDto = bulletinComplet();
        savedDto.setId(1L);

        MatiereEntity matiereEnfant = new MatiereEntity(1L, MatiereEnum.TAFFSIR_CORAN, TypeMatiereEnum.ENFANT);

        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.matiereRepository.findByType(TypeMatiereEnum.ENFANT)).thenReturn(List.of(matiereEnfant));
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        BulletinDto result = this.bulletinService.createBulletin(bulletinDto);

        // THEN
        assertNotNull(result);
        verify(this.asyncDocumentService, times(1)).requestDocumentGeneration(eq(DocumentRequestType.BULLETIN), eq(1L));
    }

    @Test
    public void testCreateBulletin_AppreciationNull_DocumentGenerationNotCalled() {
        // GIVEN
        BulletinDto savedDto = bulletinComplet();
        savedDto.setAppreciation(null);
        prepareCreateBulletinMocks(savedDto);

        // WHEN
        BulletinDto result = this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_AppreciationBlank_DocumentGenerationNotCalled() {
        // GIVEN
        BulletinDto savedDto = bulletinComplet();
        savedDto.setAppreciation("   ");
        prepareCreateBulletinMocks(savedDto);

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_NbAbsencesNull_DocumentGenerationNotCalled() {
        // GIVEN
        BulletinDto savedDto = bulletinComplet();
        savedDto.setNbAbsences(null);
        prepareCreateBulletinMocks(savedDto);

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_MoisNull_DocumentGenerationNotCalled() {
        // GIVEN
        BulletinDto savedDto = bulletinComplet();
        savedDto.setMois(null);
        prepareCreateBulletinMocks(savedDto);

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_AnneeNull_DocumentGenerationNotCalled() {
        // GIVEN
        BulletinDto savedDto = bulletinComplet();
        savedDto.setAnnee(null);
        prepareCreateBulletinMocks(savedDto);

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_DateBulletinNull_DocumentGenerationNotCalled() {
        // GIVEN
        BulletinDto savedDto = bulletinComplet();
        savedDto.setDateBulletin(null);
        prepareCreateBulletinMocks(savedDto);

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_NoteMatiereManquante_DocumentGenerationNotCalled() {
        // GIVEN — une matière ENFANT sans note dans le bulletin
        BulletinDto savedDto = bulletinComplet();
        // On retire la note de la matière
        savedDto.getBulletinMatieres().get(0).setNote(null);

        MatiereEntity matiereEnfant = new MatiereEntity(1L, MatiereEnum.TAFFSIR_CORAN, TypeMatiereEnum.ENFANT);

        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.matiereRepository.findByType(TypeMatiereEnum.ENFANT)).thenReturn(List.of(matiereEnfant));
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testCreateBulletin_AucuneMatiereEnfant_DocumentGenerationNotCalled() {
        // GIVEN — aucune matière ENFANT en base
        BulletinDto savedDto = bulletinComplet();

        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.matiereRepository.findByType(TypeMatiereEnum.ENFANT)).thenReturn(List.of());
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        verify(this.asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument peuplé dans findBulletinsByIdEleve
    // ---------------------------------------------------------------------------

    @Test
    public void testFindBulletinsByIdEleve_IdDocumentPeuple_QuandDocumentTrouve() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(10L);
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(99L);

        BulletinDto dto = new BulletinDto();
        dto.setId(10L);

        when(this.eleveService.findEleveById(anyLong())).thenReturn(new EleveDto());
        when(this.bulletinRepository.findByIdEleve(anyLong())).thenReturn(List.of(bulletinEntity));
        when(this.bulletinMapper.fromEntityToDto(bulletinEntity)).thenReturn(dto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), eq("10")))
                .thenReturn(Optional.of(documentEntity));

        // WHEN
        List<BulletinDto> result = this.bulletinService.findBulletinsByIdEleve(1L);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(99L, result.get(0).getIdDocument());
    }

    @Test
    public void testFindBulletinsByIdEleve_IdDocumentNull_QuandAucunDocument() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(10L);

        BulletinDto dto = new BulletinDto();
        dto.setId(10L);

        when(this.eleveService.findEleveById(anyLong())).thenReturn(new EleveDto());
        when(this.bulletinRepository.findByIdEleve(anyLong())).thenReturn(List.of(bulletinEntity));
        when(this.bulletinMapper.fromEntityToDto(bulletinEntity)).thenReturn(dto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), eq("10")))
                .thenReturn(Optional.empty());

        // WHEN
        List<BulletinDto> result = this.bulletinService.findBulletinsByIdEleve(1L);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getIdDocument());
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument peuplé dans createBulletin / updateBulletin
    // ---------------------------------------------------------------------------

    @Test
    public void testCreateBulletin_IdDocumentPeuple_QuandDocumentTrouve() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(5L);
        BulletinDto savedDto = new BulletinDto();
        savedDto.setId(5L);

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(42L);

        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), eq("5")))
                .thenReturn(Optional.of(documentEntity));

        // WHEN
        BulletinDto result = this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        assertEquals(42L, result.getIdDocument());
    }

    @Test
    public void testUpdateBulletin_IdDocumentPeuple_QuandDocumentTrouve() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(7L);
        bulletinEntity.setBulletinMatieres(new ArrayList<>());
        BulletinDto savedDto = new BulletinDto();
        savedDto.setId(7L);

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(55L);

        when(this.bulletinRepository.findById(7L)).thenReturn(Optional.of(bulletinEntity));
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), eq("7")))
                .thenReturn(Optional.of(documentEntity));

        // WHEN
        BulletinDto result = this.bulletinService.updateBulletin(7L, new BulletinDto());

        // THEN
        assertEquals(55L, result.getIdDocument());
    }

    // ---------------------------------------------------------------------------
    // Tests complet peuple sur BulletinDto retourne
    // ---------------------------------------------------------------------------

    @Test
    public void testFindBulletinsByIdEleve_CompletPeuple_QuandBulletinComplet() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(10L);
        BulletinDto dto = bulletinComplet();
        dto.setId(10L);
        MatiereEntity matiereEnfant = new MatiereEntity(1L, MatiereEnum.TAFFSIR_CORAN, TypeMatiereEnum.ENFANT);

        when(this.eleveService.findEleveById(anyLong())).thenReturn(new EleveDto());
        when(this.bulletinRepository.findByIdEleve(anyLong())).thenReturn(List.of(bulletinEntity));
        when(this.bulletinMapper.fromEntityToDto(bulletinEntity)).thenReturn(dto);
        when(this.matiereRepository.findByType(TypeMatiereEnum.ENFANT)).thenReturn(List.of(matiereEnfant));
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), eq("10")))
                .thenReturn(Optional.empty());

        // WHEN
        List<BulletinDto> result = this.bulletinService.findBulletinsByIdEleve(1L);

        // THEN
        assertTrue(result.get(0).getComplet());
    }

    @Test
    public void testCreateBulletin_CompletPeuple_QuandBulletinIncomplet() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        BulletinDto savedDto = new BulletinDto();
        savedDto.setId(1L);
        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());

        // WHEN
        BulletinDto result = this.bulletinService.createBulletin(new BulletinDto());

        // THEN
        assertFalse(result.getComplet());
    }

    // ---------------------------------------------------------------------------
    // Méthodes utilitaires
    // ---------------------------------------------------------------------------

    /** Construit un BulletinDto valide avec tous les champs requis par isBulletinComplet. */
    private BulletinDto bulletinComplet() {
        BulletinDto dto = new BulletinDto();
        dto.setAppreciation("Bon élève");
        dto.setNbAbsences(2);
        dto.setMois(3);
        dto.setAnnee(2025);
        dto.setDateBulletin(LocalDate.of(2025, 3, 31));
        BulletinMatiereDto bm = BulletinMatiereDto.builder()
                .code(MatiereEnum.TAFFSIR_CORAN)
                .note(NoteMatiereEnum.A)
                .build();
        dto.setBulletinMatieres(new ArrayList<>(List.of(bm)));
        return dto;
    }

    /** Configure les mocks minimaux pour createBulletin sans tenir compte de isBulletinComplet. */
    private void prepareCreateBulletinMocks(BulletinDto savedDto) {
        BulletinEntity bulletinEntity = new BulletinEntity();
        bulletinEntity.setId(1L);
        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(savedDto);
        when(this.documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKey.ID_BULLETIN), anyString()))
                .thenReturn(Optional.empty());
    }

}
