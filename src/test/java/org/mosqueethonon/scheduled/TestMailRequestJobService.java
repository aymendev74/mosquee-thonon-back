package org.mosqueethonon.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.repository.MailRequestRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestMailRequestJobService {

    @Mock
    private MailRequestRepository mailRequestRepository;

    @Mock
    private MailRequestProcessor mailRequestProcessor;

    @InjectMocks
    private MailRequestJobService mailRequestJobService;

    // -----------------------------------------------------------------------
    // Cas : aucun enregistrement PENDING disponible (file vide ou tous lockés)
    // -----------------------------------------------------------------------

    @Test
    public void testProcessNextPendingRequestReturnsFalseWhenNoPendingRecord() {
        // GIVEN
        when(mailRequestRepository.findFirstPendingWithLock()).thenReturn(Optional.empty());

        // WHEN
        boolean result = mailRequestJobService.processNextPendingRequest();

        // THEN
        assertThat(result).isFalse();
        verify(mailRequestProcessor, never()).processMailRequest(any());
    }

    // -----------------------------------------------------------------------
    // Cas : un enregistrement PENDING disponible, documents chargés via findById
    // -----------------------------------------------------------------------

    @Test
    public void testProcessNextPendingRequestReturnsTrueAndDelegatesToProcessor() {
        // GIVEN — la query native retourne l'entité sans associations
        MailRequestEntity lockedRequest = buildMailRequest(10L);
        // findByIdWithDocuments retourne la même entité (avec associations chargées)
        MailRequestEntity requestWithDocuments = buildMailRequest(10L);

        when(mailRequestRepository.findFirstPendingWithLock()).thenReturn(Optional.of(lockedRequest));
        when(mailRequestRepository.findById(10L)).thenReturn(Optional.of(requestWithDocuments));

        // WHEN
        boolean result = mailRequestJobService.processNextPendingRequest();

        // THEN
        assertThat(result).isTrue();
        verify(mailRequestRepository, times(1)).findById(10L);
        verify(mailRequestProcessor, times(1)).processMailRequest(requestWithDocuments);
    }

    // -----------------------------------------------------------------------
    // Cas : findById ne trouve pas l'entité (cas dégradé) -> on skip (warning log)
    // -----------------------------------------------------------------------

    @Test
    public void testProcessNextPendingRequestSkipWhenNotFoundById() {
        // GIVEN
        MailRequestEntity lockedRequest = buildMailRequest(20L);

        when(mailRequestRepository.findFirstPendingWithLock()).thenReturn(Optional.of(lockedRequest));
        when(mailRequestRepository.findById(20L)).thenReturn(Optional.empty());

        // WHEN
        boolean result = mailRequestJobService.processNextPendingRequest();

        // THEN
        assertThat(result).isTrue();
        verify(mailRequestProcessor, never()).processMailRequest(lockedRequest);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MailRequestEntity buildMailRequest(Long id) {
        MailRequestEntity entity = MailRequestEntity.builder()
                .type(MailRequestType.INSCRIPTION)
                .businessId(id * 10)
                .statut(MailRequestStatut.PENDING)
                .build();
        entity.setId(id);
        return entity;
    }
}
