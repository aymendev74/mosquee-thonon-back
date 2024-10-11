package org.mosqueethonon.service.impl.inscription;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.mapper.inscription.InscriptionAdulteMapper;
import org.mosqueethonon.v1.mapper.inscription.InscriptionAdulteMapperImpl;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionAdulteServiceImpl {

    @Mock
    private InscriptionAdulteRepository inscriptionAdulteRepository;

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Spy
    private InscriptionAdulteMapper inscriptionAdulteMapper = new InscriptionAdulteMapperImpl();

    @Mock
    private TarifCalculService tarifCalculService;

    @Mock
    private ParamService paramService;

    @Mock
    private MailingConfirmationRepository mailingConfirmationRepository;

    @Mock
    private Environment environment;

    @InjectMocks
    private InscriptionAdulteServiceImpl inscriptionAdulteService;

    private InscriptionAdulteDto inscriptionDto;
    private InscriptionAdulteEntity inscriptionEntity;
    private InscriptionSaveCriteria criteria;

    @BeforeEach
    public void setUp() {
        inscriptionDto = new InscriptionAdulteDto();
        inscriptionDto.setNom("Test Nom");
        inscriptionDto.setPrenom("Test Prenom");

        inscriptionEntity = new InscriptionAdulteEntity();
        inscriptionEntity.setId(1L);
        inscriptionEntity.setDateInscription(LocalDateTime.now());
        inscriptionEntity.setResponsableLegal(new ResponsableLegalEntity());

        criteria = InscriptionSaveCriteria.builder().sendMailConfirmation(true).build();
    }

    @Test
    public void testCreateInscription_Success() {
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class))).thenReturn(tarifDto);

        InscriptionAdulteDto result = inscriptionAdulteService.createInscription(inscriptionDto, criteria);

        verify(inscriptionAdulteRepository, times(1)).save(any(InscriptionAdulteEntity.class));
        verify(mailingConfirmationRepository, times(1)).save(any(MailingConfirmationEntity.class));
    }

    @Test
    public void testFindInscriptionById_NotFound() {
        // Arrange
        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.findInscriptionById(1L);

        // Assert
        assertNull(result);
        verify(inscriptionAdulteRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateInscription_Success() {
        // Arrange
        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.of(inscriptionEntity));
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(anyLong(), any(LocalDate.class))).thenReturn(tarifDto);

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.updateInscription(1L, inscriptionDto, criteria);

        // Assert
        assertNotNull(result);
        verify(inscriptionAdulteRepository, times(1)).findById(1L);
        verify(inscriptionAdulteRepository, times(1)).save(any(InscriptionAdulteEntity.class));
        verify(mailingConfirmationRepository, times(1)).save(any(MailingConfirmationEntity.class));
    }

    @Test
    public void testUpdateInscription_NotFound() {
        // Arrange
        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inscriptionAdulteService.updateInscription(1L, inscriptionDto, criteria);
        });

        assertEquals("Inscription not found ! idinsc = 1", exception.getMessage());
        verify(inscriptionAdulteRepository, times(1)).findById(1L);
        verify(inscriptionAdulteRepository, times(0)).save(any(InscriptionAdulteEntity.class));
    }

    @Test
    public void testFindNbInscriptionsByPeriode() {
        // Arrange
        when(inscriptionRepository.getNbElevesInscritsByIdPeriode(anyLong(), anyString())).thenReturn(10);

        // Act
        Integer nbInscriptions = inscriptionAdulteService.findNbInscriptionsByPeriode(1L);

        // Assert
        assertEquals(10, nbInscriptions);
        verify(inscriptionRepository, times(1)).getNbElevesInscritsByIdPeriode(anyLong(), anyString());
    }

    @Test
    public void testIsInscriptionOutsidePeriode() {
        // Arrange
        PeriodeDto periode = new PeriodeDto();
        periode.setDateDebut(LocalDate.now().minusDays(1));
        periode.setDateFin(LocalDate.now().plusDays(1));

        when(inscriptionRepository.getNbInscriptionOutsideRange(anyLong(), any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(1);

        // Act
        boolean result = inscriptionAdulteService.isInscriptionOutsidePeriode(1L, periode);

        // Assert
        assertTrue(result);
        verify(inscriptionRepository, times(1)).getNbInscriptionOutsideRange(anyLong(), any(LocalDate.class), any(LocalDate.class), anyString());
    }
}