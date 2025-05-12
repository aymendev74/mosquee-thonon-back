package org.mosqueethonon.service.impl.inscription;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionOrchestratorServiceImpl {

    @Mock
    private InscriptionEnfantService inscriptionEnfantService;

    @Mock
    private InscriptionService inscriptionService;

    @Mock
    private PeriodeService periodeService;

    @InjectMocks
    private InscriptionOrchestratorServiceImpl inscriptionOrchestratorService;

    private InscriptionEnfantDto inscriptionEnfantDto;

    private InscriptionSaveCriteria criteria;

    @BeforeEach
    public void setUp() {
        // Setup test data
        inscriptionEnfantDto = new InscriptionEnfantDto();
        inscriptionEnfantDto.setId(1L);

        criteria = InscriptionSaveCriteria.builder().build();
    }

    @Test
    @Transactional
    public void testUpdateInscription() {
        // Given
        Long id = 1L;
        when(inscriptionEnfantService.updateInscription(eq(id), eq(inscriptionEnfantDto), eq(criteria)))
                .thenReturn(inscriptionEnfantDto);
        when(inscriptionService.getIdPeriodeByIdInscription(anyLong())).thenReturn(1L);

        // When
        InscriptionEnfantDto updatedInscription = inscriptionOrchestratorService.updateInscription(id, inscriptionEnfantDto, criteria);

        // Then
        assertNotNull(updatedInscription);
        assertEquals(1L, updatedInscription.getId());
        verify(inscriptionEnfantService, times(1)).updateInscription(eq(id), eq(inscriptionEnfantDto), eq(criteria));
        verify(inscriptionService, times(1)).getIdPeriodeByIdInscription(eq(1L));
        verify(periodeService, times(1)).updateNbMaxElevesIfNeeded(eq(1L));
    }

    @Test
    @Transactional
    public void testDeleteInscriptions() {
        // Given
        Set<Long> ids = new HashSet<>();
        ids.add(1L);
        InscriptionEnfantEntity inscriptionEntity = new InscriptionEnfantEntity();
        inscriptionEntity.setId(1L);
        inscriptionEntity.setType("ENFANT");
        PeriodeEntity periode = new PeriodeEntity();
        periode.setNbMaxInscription(20);
        periode.setId(1L);

        when(inscriptionService.findInscriptionById(1L)).thenReturn(inscriptionEntity);
        when(inscriptionService.getIdPeriodeByIdInscription(1L)).thenReturn(1L);
        when(periodeService.findPeriodeById(eq(1L))).thenReturn(periode);
        // When
        Set<Long> deletedIds = inscriptionOrchestratorService.deleteInscriptions(ids);

        // Then
        assertEquals(ids, deletedIds);
        verify(inscriptionService, times(1)).deleteInscriptions(eq(ids));
        verify(inscriptionService, times(1)).findInscriptionById(eq(1L));
        verify(inscriptionService, times(1)).getIdPeriodeByIdInscription(eq(1L));
        verify(periodeService, times(1)).findPeriodeById(eq(1L));
        verify(inscriptionEnfantService, times(1)).updateListeAttente(eq(1L), eq(20));
    }

}
