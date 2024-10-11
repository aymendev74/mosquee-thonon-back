package org.mosqueethonon.service.impl.inscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.v1.dto.inscription.InscriptionPatchDto;
import org.mosqueethonon.v1.enums.StatutInscription;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionServiceImpl {

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private InscriptionEnfantService inscriptionEnfantService;

    @InjectMocks
    private InscriptionServiceImpl inscriptionService;

    @Test
    public void testPatchInscriptions_WhenInscriptionsExist() {
        // Arrange
        InscriptionPatchDto inscriptionPatchDto = new InscriptionPatchDto();
        inscriptionPatchDto.setIds(List.of(1L, 2L));
        inscriptionPatchDto.setStatut(StatutInscription.VALIDEE);

        InscriptionEntity inscription1 = new InscriptionEnfantEntity();
        inscription1.setId(1L);
        inscription1.setStatut(StatutInscription.LISTE_ATTENTE);
        inscription1.setNoPositionAttente(5);

        InscriptionEntity inscription2 = new InscriptionEnfantEntity();
        inscription2.setId(2L);
        inscription2.setStatut(StatutInscription.LISTE_ATTENTE);
        inscription2.setNoPositionAttente(10);

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(inscription1));
        when(inscriptionRepository.findById(2L)).thenReturn(Optional.of(inscription2));
        when(inscriptionRepository.saveAll(any())).thenReturn(List.of(inscription1, inscription2));

        // Act
        Set<Long> result = inscriptionService.patchInscriptions(inscriptionPatchDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertNull(inscription1.getNoPositionAttente()); // Vérifie que la position d'attente est mise à null
        assertNull(inscription2.getNoPositionAttente());
        verify(inscriptionRepository).saveAll(any());
    }

    @Test
    public void testPatchInscriptions_WhenNoInscriptionsExist() {
        // Arrange
        InscriptionPatchDto inscriptionPatchDto = new InscriptionPatchDto();
        inscriptionPatchDto.setIds(List.of(1L, 2L));
        inscriptionPatchDto.setStatut(StatutInscription.VALIDEE);

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.empty());
        when(inscriptionRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Set<Long> result = inscriptionService.patchInscriptions(inscriptionPatchDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inscriptionRepository, never()).saveAll(any());
    }

    @Test
    public void testDeleteInscriptions_WithInscriptionEnfant() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L);
        InscriptionEntity inscription1 = new InscriptionEnfantEntity();
        inscription1.setId(1L);
        inscription1.setType("ENFANT");

        InscriptionEntity inscription2 = new InscriptionAdulteEntity();
        inscription2.setId(2L);
        inscription2.setType("ADULTE");

        when(inscriptionRepository.findAllById(ids)).thenReturn(List.of(inscription1, inscription2));

        // Act
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(inscriptionRepository).deleteAllById(ids);
        verify(inscriptionEnfantService).updateListeAttentePeriode(null);
    }

    @Test
    public void testDeleteInscriptions_WithoutInscriptionEnfant() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L);
        InscriptionEntity inscription1 = new InscriptionAdulteEntity();
        inscription1.setId(1L);
        inscription1.setType("ADULTE");

        InscriptionEntity inscription2 = new InscriptionAdulteEntity();
        inscription2.setId(2L);
        inscription2.setType("ADULTE");

        when(inscriptionRepository.findAllById(ids)).thenReturn(List.of(inscription1, inscription2));

        // Act
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(inscriptionRepository).deleteAllById(ids);
        verify(inscriptionEnfantService, never()).updateListeAttentePeriode(null); // Vérifie que la méthode n'est pas appelée
    }
}