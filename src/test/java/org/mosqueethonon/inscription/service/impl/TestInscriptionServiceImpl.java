package org.mosqueethonon.inscription.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.security.context.SecurityContext;
import org.mosqueethonon.bulletin.entity.BulletinEntity;
import org.mosqueethonon.document.entity.DocumentEntity;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.inscription.entity.InscriptionAdulteEntity;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
import org.mosqueethonon.inscription.entity.InscriptionEntity;
import org.mosqueethonon.referentiel.entity.PeriodeEntity;
import org.mosqueethonon.tarif.entity.TarifEntity;
import org.mosqueethonon.document.enums.DocumentMetadataKeyEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;
import org.mosqueethonon.mail.enums.MailRequestTypeEnum;
import org.mosqueethonon.common.exception.BadRequestException;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.bulletin.repository.BulletinRepository;
import org.mosqueethonon.document.repository.DocumentRepository;
import org.mosqueethonon.document.repository.DocumentRequestRepository;
import org.mosqueethonon.classe.repository.EleveFeuillePresenceRepository;
import org.mosqueethonon.inscription.repository.InscriptionRepository;
import org.mosqueethonon.classe.repository.LienClasseEleveRepository;
import org.mosqueethonon.mail.repository.MailRequestRepository;
import org.mosqueethonon.document.service.DocumentService;
import org.mosqueethonon.inscription.service.InscriptionEnfantService;
import org.mosqueethonon.lock.service.LockService;
import org.mosqueethonon.referentiel.service.PeriodeService;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionServiceImpl {

    @Mock
    private InscriptionRepository inscriptionRepository;
    @Mock
    private InscriptionEnfantService inscriptionEnfantService;
    @Mock
    private PeriodeService periodeService;
    @Mock
    private MailRequestRepository mailRequestRepository;
    @Mock
    private LockService lockService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private BulletinRepository bulletinRepository;
    @Mock
    private EleveFeuillePresenceRepository eleveFeuillePresenceRepository;
    @Mock
    private LienClasseEleveRepository lienClasseEleveRepository;
    @Mock
    private DocumentRequestRepository documentRequestRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentService documentService;
    @InjectMocks
    private InscriptionServiceImpl inscriptionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        // Par défaut, aucun document associé aux inscriptions
        lenient().when(documentRepository.findByMetadataKeyAndValue(any(), anyString()))
                .thenReturn(Optional.empty());
    }

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
        inscription1.setStatut(StatutInscriptionEnum.LISTE_ATTENTE);
        inscription1.setNoPositionAttente(5);

        InscriptionEntity inscription2 = new InscriptionEnfantEntity();
        inscription2.setId(2L);
        inscription2.setStatut(StatutInscriptionEnum.LISTE_ATTENTE);
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
    public void testDeleteInscriptions_WithInscriptionEnfantAndBulletins() {
        // GIVEN
        TarifEntity tarif = new TarifEntity();
        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        tarif.setPeriode(periode);
        Set<Long> ids = Set.of(1L, 2L);
        EleveEntity eleve1 = new EleveEntity();
        eleve1.setId(10L);
        eleve1.setTarif(tarif);
        List<EleveEntity> eleves = new ArrayList<>();
        eleves.add(eleve1);

        InscriptionEntity inscription1 = new InscriptionEnfantEntity();
        inscription1.setId(1L);
        inscription1.setType("ENFANT");
        inscription1.setEleves(eleves);

        InscriptionEntity inscription2 = new InscriptionAdulteEntity();
        inscription2.setId(2L);
        inscription2.setType("ADULTE");
        inscription2.setEleves(eleves);

        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setId(100L);

        DocumentEntity docEntity = new DocumentEntity();
        docEntity.setId(200L);

        DocumentEntity docBulletin = new DocumentEntity();
        docBulletin.setId(300L);
        when(documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKeyEnum.ID_BULLETIN), eq("100")))
                .thenReturn(Optional.of(docBulletin));

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(inscription1));
        when(inscriptionRepository.findById(2L)).thenReturn(Optional.of(inscription2));
        when(bulletinRepository.findByIdEleveIn(anyList())).thenReturn(List.of(bulletin));
        when(documentRepository.findByMetadataKeyAndValue(eq(DocumentMetadataKeyEnum.ID_INSCRIPTION), anyString()))
                .thenReturn(Optional.of(docEntity));

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(inscriptionRepository, times(2)).deleteById(any());
        verify(mailRequestRepository, times(2)).deleteByTypeAndBusinessIdIn(eq(MailRequestTypeEnum.INSCRIPTION), any());
        verify(documentRequestRepository, times(2))
                .deleteByTypeAndBusinessIdIn(eq(DocumentRequestTypeEnum.BULLETIN), any());
        verify(bulletinRepository, times(2)).deleteAll(anyList());
        verify(documentService, times(2)).deleteDocument(200L);
        verify(documentService, times(2)).deleteDocument(300L);
    }

    @Test
    public void testDeleteInscriptions_NeSupprimeAucunDocumentBulletin_QuandAucunDocumentTrouve() {
        // GIVEN
        Set<Long> ids = Set.of(1L);
        EleveEntity eleve1 = new EleveEntity();
        eleve1.setId(10L);
        InscriptionEntity inscription1 = new InscriptionEnfantEntity();
        inscription1.setId(1L);
        inscription1.setType("ENFANT");
        inscription1.setEleves(new ArrayList<>(List.of(eleve1)));

        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setId(101L);

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(inscription1));
        when(bulletinRepository.findByIdEleveIn(anyList())).thenReturn(List.of(bulletin));
        // documentRepository retourne Optional.empty() pour ID_BULLETIN (comportement par défaut du @BeforeEach)

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        verify(documentService, never()).deleteDocument(anyLong());
        verify(bulletinRepository, times(1)).deleteAll(anyList());
    }

    @Test
    public void testDeleteInscriptions_WhenNoDocumentAssociated() {
        // GIVEN
        Set<Long> ids = Set.of(1L);
        InscriptionEntity inscription = new InscriptionAdulteEntity();
        inscription.setId(1L);
        inscription.setEleves(new ArrayList<>());

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(inscription));
        // documentRepository retourne Optional.empty() (comportement par défaut du @BeforeEach)

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        assertTrue(result.contains(1L));
        verify(documentService, never()).deleteDocument(anyLong());
        verify(inscriptionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteInscriptions_WithInscriptionEnfantAndNoBulletins() {
        // GIVEN
        TarifEntity tarif = new TarifEntity();
        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        tarif.setPeriode(periode);
        Set<Long> ids = Set.of(1L);
        EleveEntity eleve1 = new EleveEntity();
        eleve1.setId(10L);
        eleve1.setTarif(tarif);
        List<EleveEntity> eleves = List.of(eleve1);

        InscriptionEntity inscription1 = new InscriptionEnfantEntity();
        inscription1.setId(1L);
        inscription1.setType("ENFANT");
        inscription1.setEleves(eleves);

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(inscription1));
        when(bulletinRepository.findByIdEleveIn(anyList())).thenReturn(List.of());

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        verify(documentRequestRepository, never())
                .deleteByTypeAndBusinessIdIn(eq(DocumentRequestTypeEnum.BULLETIN), any());
        verify(bulletinRepository, never()).deleteAll(anyList());
    }

    @Test
    public void testDeleteInscriptions_WithoutInscriptionEnfant() {
        // GIVEN
        Set<Long> ids = Set.of(1L, 2L);
        InscriptionEntity inscription1 = new InscriptionAdulteEntity();
        inscription1.setId(1L);
        inscription1.setType("ADULTE");
        inscription1.setEleves(new ArrayList<>());

        InscriptionEntity inscription2 = new InscriptionAdulteEntity();
        inscription2.setId(2L);
        inscription2.setType("ADULTE");
        inscription2.setEleves(new ArrayList<>());

        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(inscription1));
        when(inscriptionRepository.findById(2L)).thenReturn(Optional.of(inscription2));

        // WHEN
        Set<Long> result = inscriptionService.deleteInscriptions(ids);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(inscriptionRepository,times(2)).deleteById(any());
        verify(mailRequestRepository, times(2)).deleteByTypeAndBusinessIdIn(eq(MailRequestTypeEnum.INSCRIPTION), any());
    }
}