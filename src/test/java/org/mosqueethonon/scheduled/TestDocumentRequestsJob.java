package org.mosqueethonon.scheduled;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.service.mail.MailRequestService;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TestDocumentRequestsJob {

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private DocumentRequestProcessor documentRequestProcessor;

    @Mock
    private MailRequestService mailRequestService;

    @InjectMocks
    private DocumentRequestsJob documentRequestsJob;

    @Test
    public void testProcessPendingDocumentRequestsWhenNoRequests() {
        // GIVEN
        when(documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING))
                .thenReturn(Collections.emptyList());

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — aucun traitement ne doit être délégué
        verify(documentRequestProcessor, never()).processDocumentRequest(any());
        verify(mailRequestService, never()).promoteReadyMailRequests(any());
    }

    @Test
    public void testProcessPendingDocumentRequestsDelegatesEachRequestToProcessor() {
        // GIVEN
        DocumentRequestEntity request1 = buildRequest(1L, DocumentRequestType.BULLETIN, 10L);
        DocumentRequestEntity request2 = buildRequest(2L, DocumentRequestType.BULLETIN, 20L);

        when(documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING))
                .thenReturn(List.of(request1, request2));
        // Les deux processings retournent false → pas de promotion
        when(documentRequestProcessor.processDocumentRequest(request1)).thenReturn(false);
        when(documentRequestProcessor.processDocumentRequest(request2)).thenReturn(false);

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — chaque demande est traitée indépendamment par le processor
        verify(documentRequestProcessor, times(1)).processDocumentRequest(request1);
        verify(documentRequestProcessor, times(1)).processDocumentRequest(request2);
    }

    // -----------------------------------------------------------------------
    // processAndPromote — processeur retourne true (COMPLETED)
    //                     → promoteReadyMailRequests est appelé avec l'ID du document
    // -----------------------------------------------------------------------

    @Test
    public void testProcessAndPromoteCallsPromoteWhenDocumentCompleted() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(5L, DocumentRequestType.BULLETIN, 50L);

        when(documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING))
                .thenReturn(List.of(request));
        when(documentRequestProcessor.processDocumentRequest(request)).thenReturn(true);

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — la promotion des mails est déclenchée avec l'ID exact du document
        verify(mailRequestService, times(1)).promoteReadyMailRequests(5L);
    }

    // -----------------------------------------------------------------------
    // processAndPromote — processeur retourne false (ERROR)
    //                     → promoteReadyMailRequests NON appelé
    // -----------------------------------------------------------------------

    @Test
    public void testProcessAndPromoteDoesNotCallPromoteWhenDocumentFailed() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(6L, DocumentRequestType.BULLETIN, 60L);

        when(documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING))
                .thenReturn(List.of(request));
        when(documentRequestProcessor.processDocumentRequest(request)).thenReturn(false);

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — aucune promotion ne doit être déclenchée
        verify(mailRequestService, never()).promoteReadyMailRequests(any());
    }

    // -----------------------------------------------------------------------
    // processAndPromote — mix : un document COMPLETED, un en ERROR
    //                     → promote appelé uniquement pour le COMPLETED
    // -----------------------------------------------------------------------

    @Test
    public void testProcessAndPromoteOnlyPromotesCompletedDocuments() {
        // GIVEN
        DocumentRequestEntity requestCompleted = buildRequest(7L, DocumentRequestType.BULLETIN, 70L);
        DocumentRequestEntity requestFailed = buildRequest(8L, DocumentRequestType.BULLETIN, 80L);

        when(documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING))
                .thenReturn(List.of(requestCompleted, requestFailed));
        when(documentRequestProcessor.processDocumentRequest(requestCompleted)).thenReturn(true);
        when(documentRequestProcessor.processDocumentRequest(requestFailed)).thenReturn(false);

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — promote uniquement pour le document traité avec succès
        verify(mailRequestService, times(1)).promoteReadyMailRequests(7L);
        verify(mailRequestService, never()).promoteReadyMailRequests(8L);
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
}
