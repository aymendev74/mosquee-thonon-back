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

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TestDocumentRequestsJob {

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private DocumentRequestProcessor documentRequestProcessor;

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
    }

    @Test
    public void testProcessPendingDocumentRequestsDelegatesEachRequestToProcessor() {
        // GIVEN
        DocumentRequestEntity request1 = buildRequest(1L, DocumentRequestType.BULLETIN, 10L);
        DocumentRequestEntity request2 = buildRequest(2L, DocumentRequestType.BULLETIN, 20L);

        when(documentRequestRepository.findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut.PENDING))
                .thenReturn(List.of(request1, request2));

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — chaque demande est traitée indépendamment par le processor
        verify(documentRequestProcessor, times(1)).processDocumentRequest(request1);
        verify(documentRequestProcessor, times(1)).processDocumentRequest(request2);
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
