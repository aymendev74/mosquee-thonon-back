package org.mosqueethonon.service.impl.inscription;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.impl.UserAccountManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Teste la logique de {@code AbstractInscriptionService#createMailRequest} via une
 * sous-classe concrète minimale, sans dépendances supplémentaires.
 */
@ExtendWith(MockitoExtension.class)
public class TestCommonInscriptionService {

    @Mock
    private MailRequestRepository mailRequestRepository;

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private UserAccountManager userAccountManager;

    @InjectMocks
    private CommonInscriptionService service;

    // -----------------------------------------------------------------------
    // Cas : documentRequest non null
    //       → mail créé en NOT_READY, liaison documentaire insérée
    // -----------------------------------------------------------------------

    @Test
    public void testCreateMailRequestWhenDocumentRequestProvided() {
        // GIVEN
        DocumentRequestEntity documentRequest = buildDocumentRequest(99L);

        // WHEN
        service.createMailRequest(1L, documentRequest);

        // THEN — le mail est créé en NOT_READY
        ArgumentCaptor<MailRequestEntity> captor = ArgumentCaptor.forClass(MailRequestEntity.class);
        verify(mailRequestRepository, times(1)).save(captor.capture());

        MailRequestEntity savedMailRequest = captor.getAllValues().get(0);
        assertEquals(MailRequestStatut.NOT_READY, savedMailRequest.getStatut());
        assertEquals(1L, savedMailRequest.getDocumentRequests().size());
        assertEquals(99L, savedMailRequest.getDocumentRequests().get(0).getDocumentRequestId());
    }

    // -----------------------------------------------------------------------
    // Cas : documentRequest null
    //       → mail créé directement en PENDING, aucune liaison documentaire
    // -----------------------------------------------------------------------

    @Test
    public void testCreateMailRequestWhenNoDocumentRequest() {
        // GIVEN
        MailRequestEntity savedWithId = MailRequestEntity.builder()
                .businessId(2L)
                .statut(MailRequestStatut.PENDING)
                .build();

        // WHEN
        service.createMailRequest(2L, null);

        // THEN — un seul save, en PENDING, sans liaison
        ArgumentCaptor<MailRequestEntity> captor = ArgumentCaptor.forClass(MailRequestEntity.class);
        verify(mailRequestRepository, times(1)).save(captor.capture());

        MailRequestEntity savedMailRequest = captor.getValue();
        assertEquals(MailRequestStatut.PENDING, savedMailRequest.getStatut());
        assertTrue(savedMailRequest.getDocumentRequests().isEmpty(), "Aucune liaison document ne doit être créée quand documentRequest est null");
    }

    // -----------------------------------------------------------------------
    // Cas : vérification du type de mail (INSCRIPTION)
    // -----------------------------------------------------------------------

    @Test
    public void testCreateMailRequestSetsInscriptionType() {
        // GIVEN
        MailRequestEntity savedWithId = MailRequestEntity.builder()
                .businessId(3L)
                .statut(MailRequestStatut.PENDING)
                .build();
        savedWithId.setId(30L);

        when(mailRequestRepository.save(any(MailRequestEntity.class))).thenReturn(savedWithId);

        // WHEN — pas de document (chemin le plus simple pour isoler le type)
        service.createMailRequest(3L, null);

        // THEN — le type est bien INSCRIPTION
        ArgumentCaptor<MailRequestEntity> captor = ArgumentCaptor.forClass(MailRequestEntity.class);
        verify(mailRequestRepository, times(1)).save(captor.capture());
        assertEquals(org.mosqueethonon.enums.MailRequestType.INSCRIPTION, captor.getValue().getType());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DocumentRequestEntity buildDocumentRequest(Long id) {
        DocumentRequestEntity doc = new DocumentRequestEntity();
        doc.setId(id);
        doc.setType(DocumentRequestType.BULLETIN);
        doc.setStatut(DocumentRequestStatut.PENDING);
        return doc;
    }
}
