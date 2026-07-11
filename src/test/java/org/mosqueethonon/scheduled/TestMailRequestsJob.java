package org.mosqueethonon.scheduled;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestMailRequestsJob {

    @Mock
    private MailRequestJobService mailRequestJobService;

    @InjectMocks
    private MailRequestsJob mailRequestsJob;

    // -----------------------------------------------------------------------
    // sendPendingEmails — aucune demande en attente → boucle s'arrête immédiatement
    // -----------------------------------------------------------------------

    @Test
    public void testSendPendingEmailsWhenNoPendingRequests() {
        // GIVEN — aucun enregistrement PENDING disponible
        when(mailRequestJobService.processNextPendingRequest()).thenReturn(false);

        // WHEN
        mailRequestsJob.sendPendingEmails();

        // THEN — le service est appelé une seule fois et la boucle s'arrête
        verify(mailRequestJobService, times(1)).processNextPendingRequest();
    }

    // -----------------------------------------------------------------------
    // sendPendingEmails — plusieurs demandes disponibles → toutes traitées
    // -----------------------------------------------------------------------

    @Test
    public void testSendPendingEmailsProcessesAllAvailableRequests() {
        // GIVEN — deux enregistrements disponibles, puis la file est vide
        when(mailRequestJobService.processNextPendingRequest())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // WHEN
        mailRequestsJob.sendPendingEmails();

        // THEN — le service est appelé jusqu'à épuisement de la file
        verify(mailRequestJobService, times(3)).processNextPendingRequest();
    }

    // -----------------------------------------------------------------------
    // sendPendingEmails — exception critique → job s'arrête proprement
    //                     sans propager l'exception au scheduler Spring
    // -----------------------------------------------------------------------

    @Test
    public void testSendPendingEmailsStopsOnException() {
        // GIVEN — le service lève une exception critique (ex: UnexpectedRollbackException)
        when(mailRequestJobService.processNextPendingRequest())
                .thenReturn(true)
                .thenThrow(new RuntimeException("Erreur critique simulée"));

        // WHEN — ne doit pas propager l'exception
        mailRequestsJob.sendPendingEmails();

        // THEN — le job s'arrête proprement après l'incident, sans planter le scheduler
        verify(mailRequestJobService, times(2)).processNextPendingRequest();
    }
}
