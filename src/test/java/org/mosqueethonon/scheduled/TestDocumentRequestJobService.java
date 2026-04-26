package org.mosqueethonon.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestDocumentRequestJobService {

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private DocumentRequestProcessor documentRequestProcessor;

    @Mock
    private MailRequestService mailRequestService;

    @InjectMocks
    private DocumentRequestJobService documentRequestJobService;

    // -----------------------------------------------------------------------
    // Cas : aucun enregistrement PENDING disponible (file vide ou tous lockés)
    // -----------------------------------------------------------------------

    @Test
    public void testProcessNextPendingRequestReturnsFalseWhenNoPendingRecord() {
        // GIVEN
        when(documentRequestRepository.findFirstPendingWithLock()).thenReturn(Optional.empty());

        // WHEN
        boolean result = documentRequestJobService.processNextPendingRequest();

        // THEN
        assertThat(result).isFalse();
        verify(documentRequestProcessor, never()).processDocumentRequest(any());
        verify(mailRequestService, never()).promoteReadyMailRequests(any());
    }

    // -----------------------------------------------------------------------
    // Cas : document généré avec succès (COMPLETED) → promote déclenché
    // -----------------------------------------------------------------------

    @Test
    public void testProcessNextPendingRequestPromotesMailsWhenDocumentCompleted() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(10L);
        when(documentRequestRepository.findFirstPendingWithLock()).thenReturn(Optional.of(request));
        when(documentRequestProcessor.processDocumentRequest(request)).thenReturn(true);

        // WHEN
        boolean result = documentRequestJobService.processNextPendingRequest();

        // THEN
        assertThat(result).isTrue();
        verify(documentRequestProcessor, times(1)).processDocumentRequest(request);
        verify(mailRequestService, times(1)).promoteReadyMailRequests(10L);
    }

    // -----------------------------------------------------------------------
    // Cas : génération du document en erreur (ERROR) → promote NON déclenché
    // -----------------------------------------------------------------------

    @Test
    public void testProcessNextPendingRequestDoesNotPromoteMailsWhenDocumentFailed() {
        // GIVEN
        DocumentRequestEntity request = buildRequest(20L);
        when(documentRequestRepository.findFirstPendingWithLock()).thenReturn(Optional.of(request));
        when(documentRequestProcessor.processDocumentRequest(request)).thenReturn(false);

        // WHEN
        boolean result = documentRequestJobService.processNextPendingRequest();

        // THEN
        assertThat(result).isTrue();
        verify(documentRequestProcessor, times(1)).processDocumentRequest(request);
        verify(mailRequestService, never()).promoteReadyMailRequests(any());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DocumentRequestEntity buildRequest(Long id) {
        DocumentRequestEntity request = new DocumentRequestEntity();
        request.setId(id);
        request.setType(DocumentRequestType.BULLETIN);
        request.setBusinessId(id * 10);
        request.setStatut(DocumentRequestStatut.PENDING);
        return request;
    }
}
