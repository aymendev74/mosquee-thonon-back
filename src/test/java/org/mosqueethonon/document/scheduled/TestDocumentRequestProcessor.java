package org.mosqueethonon.document.scheduled;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.adhesion.entity.AdhesionEntity;
import org.mosqueethonon.bulletin.entity.BulletinEntity;
import org.mosqueethonon.inscription.entity.InscriptionAdulteEntity;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
import org.mosqueethonon.document.entity.DocumentEntity;
import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.document.enums.DocumentRequestStatutEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;
import org.mosqueethonon.adhesion.repository.AdhesionRepository;
import org.mosqueethonon.bulletin.repository.BulletinRepository;
import org.mosqueethonon.document.repository.DocumentRequestRepository;
import org.mosqueethonon.inscription.repository.InscriptionAdulteRepository;
import org.mosqueethonon.inscription.repository.InscriptionEnfantRepository;
import org.mosqueethonon.document.service.DocumentService;
import org.mosqueethonon.document.service.impl.AdhesionDocumentGenerator;
import org.mosqueethonon.document.service.impl.BulletinDocumentGenerator;
import org.mosqueethonon.document.service.impl.InscriptionAdulteDocumentGenerator;
import org.mosqueethonon.document.service.impl.InscriptionEnfantDocumentGenerator;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestDocumentRequestProcessor {

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private InscriptionAdulteDocumentGenerator inscriptionAdulteDocumentGenerator;

    @Mock
    private InscriptionAdulteRepository inscriptionAdulteRepository;

    @Mock
    private InscriptionEnfantDocumentGenerator inscriptionEnfantDocumentGenerator;

    @Mock
    private InscriptionEnfantRepository inscriptionEnfantRepository;

    @Mock
    private AdhesionDocumentGenerator adhesionDocumentGenerator;

    @Mock
    private AdhesionRepository adhesionRepository;

    @Mock
    private BulletinDocumentGenerator bulletinDocumentGenerator;

    @Mock
    private BulletinRepository bulletinRepository;

    @InjectMocks
    private DocumentRequestProcessor documentRequestProcessor;

    // -----------------------------------------------------------------------
    // case BULLETIN — succès
    // -----------------------------------------------------------------------

    @Test
    public void testProcessBulletinRequestSucces() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(1L, DocumentRequestTypeEnum.BULLETIN, 42L);
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setId(42L);
        DocumentEntity document = buildDocument("/path/bulletin-42.pdf", "BULLETIN-001");

        when(bulletinRepository.findById(42L)).thenReturn(Optional.of(bulletin));
        when(documentService.generateOrUpdateDocument(bulletinDocumentGenerator, bulletin)).thenReturn(document);

        // WHEN
        documentRequestProcessor.processDocumentRequest(request);

        // THEN
        verify(bulletinRepository, times(1)).findById(42L);
        verify(documentService, times(1)).generateOrUpdateDocument(bulletinDocumentGenerator, bulletin);

        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        DocumentRequestEntity saved = captor.getValue();
        assertEquals(DocumentRequestStatutEnum.COMPLETED, saved.getStatut());
        assertEquals("/path/bulletin-42.pdf", saved.getDocumentPath());
        assertEquals("BULLETIN-001", saved.getDocumentCode());
    }

    // -----------------------------------------------------------------------
    // case BULLETIN — bulletin non trouvé
    // -----------------------------------------------------------------------

    @Test
    public void testProcessBulletinRequestWhenBulletinNotFound() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(2L, DocumentRequestTypeEnum.BULLETIN, 999L);
        when(bulletinRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN
        documentRequestProcessor.processDocumentRequest(request);

        // THEN — l'exception est interceptée, la demande passe en erreur
        verify(documentService, never()).generateOrUpdateDocument(any(), any());

        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        DocumentRequestEntity saved = captor.getValue();
        assertEquals(DocumentRequestStatutEnum.ERROR, saved.getStatut());
        assertNotNull(saved.getErrorMessage());
        assertTrue(saved.getErrorMessage().contains("999"));
    }

    // -----------------------------------------------------------------------
    // case BULLETIN — erreur générique lors de la génération
    // -----------------------------------------------------------------------

    @Test
    public void testProcessBulletinRequestWhenGenerationThrowsException() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(3L, DocumentRequestTypeEnum.BULLETIN, 42L);
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setId(42L);

        when(bulletinRepository.findById(42L)).thenReturn(Optional.of(bulletin));
        when(documentService.generateOrUpdateDocument(bulletinDocumentGenerator, bulletin))
                .thenThrow(new RuntimeException("Erreur inattendue lors de la génération"));

        // WHEN
        documentRequestProcessor.processDocumentRequest(request);

        // THEN — la demande passe en erreur avec le message de l'exception
        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        DocumentRequestEntity saved = captor.getValue();
        assertEquals(DocumentRequestStatutEnum.ERROR, saved.getStatut());
        assertEquals("Erreur inattendue lors de la génération", saved.getErrorMessage());
    }

    // -----------------------------------------------------------------------
    // isolation des erreurs — une demande en erreur n'affecte pas la suivante
    // -----------------------------------------------------------------------

    @Test
    public void testProcessRequestsAreSelfContainedOnError() {
        // GIVEN — demande dont le bulletin est absent
        DocumentRequestEntity requestErreur = buildRequest(11L, DocumentRequestTypeEnum.BULLETIN, 999L);
        when(bulletinRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN — on traite uniquement la demande en erreur
        documentRequestProcessor.processDocumentRequest(requestErreur);

        // THEN — elle passe en ERROR sans affecter d'autres demandes
        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        assertEquals(DocumentRequestStatutEnum.ERROR, captor.getValue().getStatut());
    }

    // -----------------------------------------------------------------------
    // case INSCRIPTION_ENFANT
    // -----------------------------------------------------------------------

    @Test
    public void testProcessInscriptionEnfantRequestSucces() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(4L, DocumentRequestTypeEnum.INSCRIPTION_ENFANT, 10L);
        InscriptionEnfantEntity inscription = new InscriptionEnfantEntity();
        inscription.setId(10L);
        DocumentEntity document = buildDocument("/path/inscription-enfant-10.pdf", "INSC-ENF-001");

        when(inscriptionEnfantRepository.findById(10L)).thenReturn(Optional.of(inscription));
        when(documentService.generateOrUpdateDocument(inscriptionEnfantDocumentGenerator, inscription)).thenReturn(document);

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertTrue(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.COMPLETED, saved.getStatut());
        assertEquals("/path/inscription-enfant-10.pdf", saved.getDocumentPath());
        assertEquals("INSC-ENF-001", saved.getDocumentCode());
    }

    @Test
    public void testProcessInscriptionEnfantRequestWhenInscriptionNotFound() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(5L, DocumentRequestTypeEnum.INSCRIPTION_ENFANT, 888L);
        when(inscriptionEnfantRepository.findById(888L)).thenReturn(Optional.empty());

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertFalse(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.ERROR, saved.getStatut());
        assertTrue(saved.getErrorMessage().contains("888"));
        verify(documentService, never()).generateOrUpdateDocument(any(), any());
    }

    // -----------------------------------------------------------------------
    // case INSCRIPTION_ADULTE
    // -----------------------------------------------------------------------

    @Test
    public void testProcessInscriptionAdulteRequestSucces() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(6L, DocumentRequestTypeEnum.INSCRIPTION_ADULTE, 20L);
        InscriptionAdulteEntity inscription = new InscriptionAdulteEntity();
        inscription.setId(20L);
        DocumentEntity document = buildDocument("/path/inscription-adulte-20.pdf", "INSC-ADU-001");

        when(inscriptionAdulteRepository.findById(20L)).thenReturn(Optional.of(inscription));
        when(documentService.generateOrUpdateDocument(inscriptionAdulteDocumentGenerator, inscription)).thenReturn(document);

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertTrue(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.COMPLETED, saved.getStatut());
        assertEquals("INSC-ADU-001", saved.getDocumentCode());
    }

    @Test
    public void testProcessInscriptionAdulteRequestWhenInscriptionNotFound() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(7L, DocumentRequestTypeEnum.INSCRIPTION_ADULTE, 777L);
        when(inscriptionAdulteRepository.findById(777L)).thenReturn(Optional.empty());

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertFalse(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.ERROR, saved.getStatut());
        assertTrue(saved.getErrorMessage().contains("777"));
    }

    // -----------------------------------------------------------------------
    // case ADHESION
    // -----------------------------------------------------------------------

    @Test
    public void testProcessAdhesionRequestSucces() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(8L, DocumentRequestTypeEnum.ADHESION, 30L);
        AdhesionEntity adhesion = new AdhesionEntity();
        adhesion.setId(30L);
        DocumentEntity document = buildDocument("/path/adhesion-30.pdf", "ADH-001");

        when(adhesionRepository.findById(30L)).thenReturn(Optional.of(adhesion));
        when(documentService.generateOrUpdateDocument(adhesionDocumentGenerator, adhesion)).thenReturn(document);

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertTrue(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.COMPLETED, saved.getStatut());
        assertEquals("ADH-001", saved.getDocumentCode());
    }

    @Test
    public void testProcessAdhesionRequestWhenAdhesionNotFound() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(9L, DocumentRequestTypeEnum.ADHESION, 666L);
        when(adhesionRepository.findById(666L)).thenReturn(Optional.empty());

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertFalse(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.ERROR, saved.getStatut());
        assertTrue(saved.getErrorMessage().contains("666"));
    }

    // -----------------------------------------------------------------------
    // type non géré
    // -----------------------------------------------------------------------

    @Test
    public void testProcessRequestWithoutTypeIsRejected() {
        // GIVEN — aucun type renseigné : aucun traitement ne peut être choisi
        DocumentRequestEntity request = buildRequest(10L, null, 1L);

        // WHEN
        boolean result = documentRequestProcessor.processDocumentRequest(request);

        // THEN
        assertFalse(result);
        DocumentRequestEntity saved = captureSavedRequest();
        assertEquals(DocumentRequestStatutEnum.ERROR, saved.getStatut());
        assertNotNull(saved.getErrorMessage());
        verify(documentService, never()).generateOrUpdateDocument(any(), any());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DocumentRequestEntity captureSavedRequest() {
        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        return captor.getValue();
    }

    private DocumentRequestEntity buildRequest(Long id, DocumentRequestTypeEnum type, Long businessId) {
        DocumentRequestEntity request = new DocumentRequestEntity();
        request.setId(id);
        request.setType(type);
        request.setBusinessId(businessId);
        request.setStatut(DocumentRequestStatutEnum.PENDING);
        return request;
    }

    private DocumentEntity buildDocument(String chemin, String code) {
        DocumentEntity document = new DocumentEntity();
        document.setChemin(chemin);
        document.setCode(code);
        return document;
    }
}
