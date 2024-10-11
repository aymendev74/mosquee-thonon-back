package org.mosqueethonon.service.impl.referentiel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.PeriodeInfoEntity;
import org.mosqueethonon.repository.PeriodeInfoRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeValidationResultDto;
import org.mosqueethonon.v1.mapper.referentiel.PeriodeInfoMapper;
import org.mosqueethonon.v1.mapper.referentiel.PeriodeInfoMapperImpl;
import org.mosqueethonon.v1.mapper.referentiel.PeriodeMapper;
import org.mosqueethonon.v1.mapper.referentiel.PeriodeMapperImpl;

@ExtendWith(MockitoExtension.class)
public class TestPeriodeServiceImpl {

    @Mock
    private PeriodeInfoRepository periodeInfoRepository;

    @Mock
    private PeriodeRepository periodeRepository;

    @Spy
    private PeriodeInfoMapper periodeInfoMapper = new PeriodeInfoMapperImpl();

    @Spy
    private PeriodeMapper periodeMapper = new PeriodeMapperImpl();

    @Mock
    private InscriptionEnfantService inscriptionEnfantService;

    @Mock
    private InscriptionAdulteService inscriptionAdulteService;

    @InjectMocks
    private PeriodeServiceImpl periodeService;

    @Test
    public void testFindPeriodesByApplication_WhenExists() {
        String application = "COURS_ENFANT";
        PeriodeInfoEntity entity = new PeriodeInfoEntity();
        entity.setApplication(application);
        List<PeriodeInfoEntity> entities = Collections.singletonList(entity);

        when(periodeInfoRepository.findByApplicationOrderByDateDebutDesc(application)).thenReturn(entities);
        when(periodeInfoMapper.fromEntityToDto(entity)).thenReturn(new PeriodeInfoDto());

        List<PeriodeInfoDto> result = periodeService.findPeriodesByApplication(application);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(periodeInfoRepository).findByApplicationOrderByDateDebutDesc(application);
    }

    @Test
    public void testFindPeriodesByApplication_WhenNotExists() {
        String application = "COURS_ENFANT";
        when(periodeInfoRepository.findByApplicationOrderByDateDebutDesc(application)).thenReturn(Collections.emptyList());

        List<PeriodeInfoDto> result = periodeService.findPeriodesByApplication(application);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreatePeriode() {
        PeriodeDto periodeDto = new PeriodeDto();
        PeriodeEntity periodeEntity = new PeriodeEntity();
        when(periodeMapper.mapDtoToEntity(periodeDto, periodeEntity)).thenReturn(periodeEntity);
        when(periodeRepository.save(periodeEntity)).thenReturn(periodeEntity);
        when(periodeMapper.fromEntityToDto(periodeEntity)).thenReturn(periodeDto);

        PeriodeDto result = periodeService.createPeriode(periodeDto);

        assertNotNull(result);
        verify(periodeRepository).save(any(PeriodeEntity.class));
    }

    @Test
    public void testUpdatePeriode_WhenExists() {
        // Arrange
        Long id = 1L;
        PeriodeDto periodeDto = new PeriodeDto();
        PeriodeEntity existingEntity = new PeriodeEntity();
        when(periodeRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(periodeMapper.mapDtoToEntity(periodeDto, existingEntity)).thenReturn(existingEntity);
        when(periodeRepository.save(existingEntity)).thenReturn(existingEntity);
        when(periodeMapper.fromEntityToDto(existingEntity)).thenReturn(periodeDto);

        // Act
        PeriodeDto result = periodeService.updatePeriode(id, periodeDto);

        // Assert
        assertNotNull(result);
        verify(periodeRepository).findById(id);
        verify(inscriptionEnfantService).updateListeAttentePeriode(id);
    }

    @Test
    public void testUpdatePeriode_WhenNotExists() {
        Long id = 1L;
        PeriodeDto periodeDto = new PeriodeDto();
        when(periodeRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            periodeService.updatePeriode(id, periodeDto);
        });
        assertEquals("Periode non trouvée ! idperi = " + id, exception.getMessage());
    }

    @Test
    public void testValidatePeriode_Success() {
        // Arrange
        Long id = 1L;
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setApplication("COURS_ENFANT");
        periodeDto.setNbMaxInscription(20);

        when(inscriptionEnfantService.isInscriptionOutsidePeriode(id, periodeDto)).thenReturn(false);
        when(inscriptionEnfantService.findNbInscriptionsByPeriode(id)).thenReturn(0);
        when(periodeRepository.findByApplicationAndIdNot(periodeDto.getApplication(), id)).thenReturn(Collections.emptyList());

        PeriodeValidationResultDto result = periodeService.validatePeriode(id, periodeDto);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidatePeriode_Overlap() {
        // Arrange
        Long id = 1L;
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setDateDebut(LocalDate.of(2020, 1, 1));
        periodeDto.setDateFin(LocalDate.of(2020, 12, 31));
        periodeDto.setApplication("COURS_ENFANT");

        PeriodeEntity periodeEntity = new PeriodeEntity();
        periodeEntity.setDateDebut(LocalDate.of(2020, 2, 1));
        periodeEntity.setDateFin(LocalDate.of(2020, 3, 31));

        when(periodeRepository.findByApplicationAndIdNot(periodeDto.getApplication(), id)).thenReturn(Collections.singletonList(periodeEntity));

        // Act
        PeriodeValidationResultDto result = periodeService.validatePeriode(id, periodeDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("OVERLAP", result.getErrorCode());
    }
}