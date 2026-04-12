package org.mosqueethonon.scheduled;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionEnfantRepository;
import org.mosqueethonon.service.document.DocumentService;
import org.mosqueethonon.service.impl.document.AdhesionDocumentGenerator;
import org.mosqueethonon.service.impl.document.BulletinDocumentGenerator;
import org.mosqueethonon.service.impl.document.InscriptionAdulteDocumentGenerator;
import org.mosqueethonon.service.impl.document.InscriptionEnfantDocumentGenerator;

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
        DocumentRequestEntity request = buildRequest(1L, DocumentRequestType.BULLETIN, 42L);
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
        assertEquals(DocumentRequestStatut.COMPLETED, saved.getStatut());
        assertEquals("/path/bulletin-42.pdf", saved.getDocumentPath());
        assertEquals("BULLETIN-001", saved.getDocumentCode());
    }

    // -----------------------------------------------------------------------
    // case BULLETIN — bulletin non trouvé
    // -----------------------------------------------------------------------

    @Test
    public void testProcessBulletinRequestWhenBulletinNotFound() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(2L, DocumentRequestType.BULLETIN, 999L);
        when(bulletinRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN
        documentRequestProcessor.processDocumentRequest(request);

        // THEN — l'exception est interceptée, la demande passe en erreur
        verify(documentService, never()).generateOrUpdateDocument(any(), any());

        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        DocumentRequestEntity saved = captor.getValue();
        assertEquals(DocumentRequestStatut.ERROR, saved.getStatut());
        assertNotNull(saved.getErrorMessage());
        assertTrue(saved.getErrorMessage().contains("999"));
    }

    // -----------------------------------------------------------------------
    // case BULLETIN — erreur générique lors de la génération
    // -----------------------------------------------------------------------

    @Test
    public void testProcessBulletinRequestWhenGenerationThrowsException() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(3L, DocumentRequestType.BULLETIN, 42L);
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
        assertEquals(DocumentRequestStatut.ERROR, saved.getStatut());
        assertEquals("Erreur inattendue lors de la génération", saved.getErrorMessage());
    }

    // -----------------------------------------------------------------------
    // isolation des erreurs — une demande en erreur n'affecte pas la suivante
    // -----------------------------------------------------------------------

    @Test
    public void testProcessRequestsAreSelfContainedOnError() {
        // GIVEN — demande dont le bulletin est absent
        DocumentRequestEntity requestErreur = buildRequest(11L, DocumentRequestType.BULLETIN, 999L);
        when(bulletinRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN — on traite uniquement la demande en erreur
        documentRequestProcessor.processDocumentRequest(requestErreur);

        // THEN — elle passe en ERROR sans affecter d'autres demandes
        ArgumentCaptor<DocumentRequestEntity> captor = ArgumentCaptor.forClass(DocumentRequestEntity.class);
        verify(documentRequestRepository, times(1)).save(captor.capture());
        assertEquals(DocumentRequestStatut.ERROR, captor.getValue().getStatut());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DocumentRequestEntity buildRequest(Long id, DocumentRequestType type, Long businessId) {
        DocumentRequestEntity request = new DocumentRequestEntity();
        request.setId(id);
        request.setType(type);
        request.setBusinessId(businessId);
        request.setStatut(DocumentRequestStatut.PENDING);
        return request;
    }

    private DocumentEntity buildDocument(String chemin, String code) {
        DocumentEntity document = new DocumentEntity();
        document.setChemin(chemin);
        document.setCode(code);
        return document;
    }
}
