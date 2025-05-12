package org.mosqueethonon.service.impl.inscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.v1.enums.StatutInscription;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionServiceImpl {

    @Mock
    private InscriptionRepository inscriptionRepository;
    @Mock
    private InscriptionEnfantService inscriptionEnfantService;
    @Mock
    PeriodeService periodeService;
    @InjectMocks
    private InscriptionServiceImpl inscriptionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testPatchInscriptions_WhenInscriptionsExist() {
        // GIVEN
        ObjectNode inscriptions = this.objectMapper.createObjectNode();
        ArrayNode inscriptionsArray = inscriptions.putArray("inscriptions");
        ObjectNode inscriptionNode1 = this.objectMapper.createObjectNode();
        inscriptionNode1.put("id", 1L);
        inscriptionNode1.put("statut", "VALIDEE");
        inscriptionsArray.add(inscriptionNode1);
        ObjectNode inscriptionNode2 = this.objectMapper.createObjectNode();
        inscriptionNode2.put("id", 2L);
        inscriptionNode2.put("statut", "VALIDEE");
        inscriptionsArray.add(inscriptionNode2);

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

        // WHEN
        Set<Long> result = inscriptionService.patchInscriptions(inscriptions);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertNull(inscription1.getNoPositionAttente()); // Vérifie que la position d'attente est mise à null
        assertNull(inscription2.getNoPositionAttente());
        verify(inscriptionRepository, times(2)).save(any());
    }

    @Test
    public void testPatchInscriptions_WhenNoInscriptionsExist() {
        // GIVEN
        ObjectNode inscriptions = this.objectMapper.createObjectNode();
        ArrayNode inscriptionsArray = inscriptions.putArray("inscriptions");
        ObjectNode inscriptionNode1 = this.objectMapper.createObjectNode();
        inscriptionNode1.put("id", 1L);
        inscriptionNode1.put("statut", "VALIDEE");
        inscriptionsArray.add(inscriptionNode1);

        // WHEN
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // THEN
        assertThrows(ResourceNotFoundException.class, () -> inscriptionService.patchInscriptions(inscriptions));
    }

    @Test
    public void testDeleteInscriptions_WithInscriptionEnfant() {
        // GIVEN
        TarifEntity tarif = new TarifEntity();
        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        tarif.setPeriode(periode);
        Set<Long> ids = Set.of(1L, 2L);
        List<EleveEntity> eleves = new ArrayList<>();
        EleveEntity eleve1 = new EleveEntity();
        eleve1.setTarif(tarif);
        eleves.add(eleve1);
        InscriptionEntity inscription1 = new InscriptionEnfantEntity();
        inscription1.setId(1L);
        inscription1.setType("ENFANT");
        inscription1.setEleves(eleves);

        InscriptionEntity inscription2 = new InscriptionAdulteEntity();
        inscription2.setId(2L);
        inscription2.setType("ADULTE");
        inscription2.setEleves(eleves);

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(inscriptionRepository).deleteAllById(ids);
    }

    @Test
    public void testDeleteInscriptions_WithoutInscriptionEnfant() {
        // GIVEN
        Set<Long> ids = Set.of(1L, 2L);
        InscriptionEntity inscription1 = new InscriptionAdulteEntity();
        inscription1.setId(1L);
        inscription1.setType("ADULTE");

        InscriptionEntity inscription2 = new InscriptionAdulteEntity();
        inscription2.setId(2L);
        inscription2.setType("ADULTE");

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(inscriptionRepository).deleteAllById(ids);
    }
}