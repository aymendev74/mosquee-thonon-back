package org.mosqueethonon.scheduled;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestDocumentRequestsJob {

    @Mock
    private DocumentRequestJobService documentRequestJobService;

    @InjectMocks
    private DocumentRequestsJob documentRequestsJob;

    @Test
    public void testProcessPendingDocumentRequestsWhenNoRequests() {
        // GIVEN — aucun enregistrement PENDING disponible
        when(documentRequestJobService.processNextPendingRequest()).thenReturn(false);

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — le service est appelé une seule fois et la boucle s'arrête
        verify(documentRequestJobService, times(1)).processNextPendingRequest();
    }

    @Test
    public void testProcessPendingDocumentRequestsProcessesAllAvailableRequests() {
        // GIVEN — deux enregistrements disponibles, puis la file est vide
        when(documentRequestJobService.processNextPendingRequest())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // WHEN
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — le service est appelé jusqu'à épuisement de la file
        verify(documentRequestJobService, times(3)).processNextPendingRequest();
    }

    @Test
    public void testProcessPendingDocumentRequestsStopsOnException() {
        // GIVEN — le service lève une exception critique (ex: UnexpectedRollbackException)
        when(documentRequestJobService.processNextPendingRequest())
                .thenReturn(true)
                .thenThrow(new RuntimeException("Erreur critique simulée"));

        // WHEN — ne doit pas propager l'exception
        documentRequestsJob.processPendingDocumentRequests();

        // THEN — le job s'arrête proprement après l'incident, sans planter le scheduler
        verify(documentRequestJobService, times(2)).processNextPendingRequest();
    }
}
