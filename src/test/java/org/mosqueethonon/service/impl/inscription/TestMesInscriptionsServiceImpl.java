package org.mosqueethonon.service.impl.inscription;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.v1.dto.inscription.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestMesInscriptionsServiceImpl {

    @Mock
    private InscriptionEnfantService inscriptionEnfantService;

    @Mock
    private InscriptionAdulteService inscriptionAdulteService;

    @InjectMocks
    private MesInscriptionsServiceImpl mesInscriptionsService;

    @Test
    public void testFindMesInscriptions_WithBothTypes() {
        // Arrange
        List<InscriptionEnfantParAnneeScolaireDto> inscriptionsEnfants = new ArrayList<>();
        inscriptionsEnfants.add(InscriptionEnfantParAnneeScolaireDto.builder().build());

        List<InscriptionAdulteParAnneeScolaireDto> inscriptionsAdultes = new ArrayList<>();
        inscriptionsAdultes.add(InscriptionAdulteParAnneeScolaireDto.builder().build());

        when(inscriptionEnfantService.findInscriptionsByUtilisateurConnecte()).thenReturn(inscriptionsEnfants);
        when(inscriptionAdulteService.findInscriptionsByUtilisateurConnecte()).thenReturn(inscriptionsAdultes);

        // Act
        MesInscriptionsDto result = mesInscriptionsService.findMesInscriptions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getInscriptionsEnfants().size());
        assertEquals(1, result.getInscriptionsAdultes().size());
    }

    @Test
    public void testFindMesInscriptions_OnlyEnfants() {
        // Arrange
        List<InscriptionEnfantParAnneeScolaireDto> inscriptionsEnfants = new ArrayList<>();
        inscriptionsEnfants.add(InscriptionEnfantParAnneeScolaireDto.builder().build());

        when(inscriptionEnfantService.findInscriptionsByUtilisateurConnecte()).thenReturn(inscriptionsEnfants);
        when(inscriptionAdulteService.findInscriptionsByUtilisateurConnecte()).thenReturn(new ArrayList<>());

        // Act
        MesInscriptionsDto result = mesInscriptionsService.findMesInscriptions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getInscriptionsEnfants().size());
        assertEquals(0, result.getInscriptionsAdultes().size());
    }

    @Test
    public void testFindMesInscriptions_Empty() {
        // Arrange
        when(inscriptionEnfantService.findInscriptionsByUtilisateurConnecte()).thenReturn(new ArrayList<>());
        when(inscriptionAdulteService.findInscriptionsByUtilisateurConnecte()).thenReturn(new ArrayList<>());

        // Act
        MesInscriptionsDto result = mesInscriptionsService.findMesInscriptions();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getInscriptionsEnfants().size());
        assertEquals(0, result.getInscriptionsAdultes().size());
    }
}
