package org.mosqueethonon.service.impl.mail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.repository.MailRequestDocumentRequestRepository;
import org.mosqueethonon.repository.MailRequestRepository;

@ExtendWith(MockitoExtension.class)
public class TestMailRequestServiceImpl {

    @Mock
    private MailRequestDocumentRequestRepository mailRequestDocumentRequestRepository;

    @Mock
    private MailRequestRepository mailRequestRepository;

    @InjectMocks
    private MailRequestServiceImpl mailRequestService;

    // -----------------------------------------------------------------------
    // Cas : aucun mail NOT_READY lié au document → rien ne se passe
    // -----------------------------------------------------------------------

    @Test
    public void testPromoteReadyMailRequestsWhenNoLinkedMailRequest() {
        // GIVEN
        when(mailRequestDocumentRequestRepository.findReadyMailRequestIds(
                eq(10L), eq("NOT_READY"), eq("COMPLETED")))
                .thenReturn(Collections.emptyList());

        // WHEN
        mailRequestService.promoteReadyMailRequests(10L);

        // THEN — aucune demande de mail n'est chargée ni sauvegardée
        verify(mailRequestRepository, never()).findAllById(any());
        verify(mailRequestRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Cas : un mail NOT_READY lié, tous les documents COMPLETED → mail passe en PENDING
    // -----------------------------------------------------------------------

    @Test
    public void testPromoteReadyMailRequestsWhenAllDocumentsCompleted() {
        // GIVEN
        Long mailRequestId = 1L;
        MailRequestEntity mailRequest = buildMailRequest(mailRequestId, MailRequestStatut.NOT_READY);

        when(mailRequestDocumentRequestRepository.findReadyMailRequestIds(
                eq(42L), eq("NOT_READY"), eq("COMPLETED")))
                .thenReturn(List.of(mailRequestId));
        when(mailRequestRepository.findAllById(List.of(mailRequestId)))
                .thenReturn(List.of(mailRequest));

        // WHEN
        mailRequestService.promoteReadyMailRequests(42L);

        // THEN — le mail passe en PENDING
        verify(mailRequestRepository, times(1)).saveAll(anyList());
        assertEquals(MailRequestStatut.PENDING, mailRequest.getStatut());
    }

    // -----------------------------------------------------------------------
    // Cas : double-vérification du statut — mail déjà passé en PENDING (concurrence)
    //        La requête SQL retourne l'ID mais la re-lecture en base révèle PENDING
    //        → le mail ne doit pas être sauvegardé une seconde fois
    // -----------------------------------------------------------------------

    @Test
    public void testPromoteReadyMailRequestsWhenMailAlreadyPendingConcurrently() {
        // GIVEN — la requête SQL renvoie l'ID (race condition entre la requête et le rechargement)
        Long mailRequestId = 2L;
        MailRequestEntity mailRequest = buildMailRequest(mailRequestId, MailRequestStatut.PENDING);

        when(mailRequestDocumentRequestRepository.findReadyMailRequestIds(
                eq(42L), eq("NOT_READY"), eq("COMPLETED")))
                .thenReturn(List.of(mailRequestId));
        when(mailRequestRepository.findAllById(List.of(mailRequestId)))
                .thenReturn(List.of(mailRequest));

        // WHEN
        mailRequestService.promoteReadyMailRequests(42L);

        // THEN — la double-vérification du statut empêche un second save
        verify(mailRequestRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Cas : plusieurs mails NOT_READY tous prêts → tous passent en PENDING
    // -----------------------------------------------------------------------

    @Test
    public void testPromoteReadyMailRequestsWhenMultipleMailsAllReady() {
        // GIVEN
        MailRequestEntity mail1 = buildMailRequest(11L, MailRequestStatut.NOT_READY);
        MailRequestEntity mail2 = buildMailRequest(12L, MailRequestStatut.NOT_READY);

        when(mailRequestDocumentRequestRepository.findReadyMailRequestIds(
                eq(7L), eq("NOT_READY"), eq("COMPLETED")))
                .thenReturn(List.of(11L, 12L));
        when(mailRequestRepository.findAllById(List.of(11L, 12L)))
                .thenReturn(List.of(mail1, mail2));

        // WHEN
        mailRequestService.promoteReadyMailRequests(7L);

        // THEN — les deux mails passent en PENDING
        verify(mailRequestRepository, times(1)).saveAll(anyList());
        assertEquals(MailRequestStatut.PENDING, mail1.getStatut());
        assertEquals(MailRequestStatut.PENDING, mail2.getStatut());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MailRequestEntity buildMailRequest(Long id, MailRequestStatut statut) {
        MailRequestEntity entity = new MailRequestEntity();
        entity.setId(id);
        entity.setStatut(statut);
        return entity;
    }
}
