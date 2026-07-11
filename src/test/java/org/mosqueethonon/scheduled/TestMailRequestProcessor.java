package org.mosqueethonon.scheduled;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.dto.mail.MailAttachmentDto;
import org.mosqueethonon.dto.mail.MailDto;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.mail.MailRequestDocumentRequestEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.exception.PendingDocumentGenerationException;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.mail.MailService;
import org.mosqueethonon.service.param.ParamService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TestMailRequestProcessor {

    @Mock
    private JavaMailSender emailSender;

    // Deux mocks distincts pour les deux qualifiers de MailService.
    // @InjectMocks ne gère pas les @Qualifier : Mockito injecte par type en premier,
    // ce qui peut inverser les deux MailService selon l'ordre d'exécution des tests.
    // On instancie donc le processor manuellement dans @BeforeEach.
    @Mock
    private MailService mailInscriptionService;

    @Mock
    private MailService mailAdhesionService;

    @Mock
    private MailRequestRepository mailRequestRepository;

    @Mock
    private DocumentRequestRepository documentRequestRepository;

    @Mock
    private ParamService paramService;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    private MailRequestProcessor mailRequestProcessor;

    @BeforeEach
    void setUp() {
        mailRequestProcessor = new MailRequestProcessor(
                emailSender,
                mailInscriptionService,
                mailAdhesionService,
                mailRequestRepository,
                documentRequestRepository,
                paramService,
                applicationConfiguration
        );
    }

    // -----------------------------------------------------------------------
    // processMailRequest — envoi de mail désactivé → mail passe en IGNORED
    // -----------------------------------------------------------------------

    @Test
    public void testProcessMailRequestWhenEmailSendingDisabled() {
        // GIVEN
        MailRequestEntity mailRequest = buildMailRequest(5L, MailRequestType.INSCRIPTION, Collections.emptyList());
        when(paramService.isSendEmailEnabled()).thenReturn(false);

        // WHEN
        mailRequestProcessor.processMailRequest(mailRequest);

        // THEN — mail passe en IGNORED, aucun envoi réel
        verify(emailSender, never()).send(any(MimeMessage.class));
        ArgumentCaptor<MailRequestEntity> captor = ArgumentCaptor.forClass(MailRequestEntity.class);
        verify(mailRequestRepository, times(1)).save(captor.capture());
        assertEquals(MailRequestStatut.IGNORED, captor.getValue().getStatut());
    }

    // -----------------------------------------------------------------------
    // processMailRequest — envoi réussi → mail passe en SENT
    // -----------------------------------------------------------------------

    @Test
    public void testProcessMailRequestSendsMailAndSetsStatusSent() {
        // GIVEN
        MailRequestEntity mailRequest = buildMailRequest(6L, MailRequestType.INSCRIPTION, Collections.emptyList());
        MailDto mailDto = buildMailDto("Confirmation inscription");
        // MimeMessage mocké pour éviter les problèmes de session JavaMail dans les tests unitaires :
        // MimeMessageHelper appelle des méthodes internes (setRecipient, setSubject...)
        // qui requièrent une session valide sur un vrai MimeMessage.
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(paramService.isSendEmailEnabled()).thenReturn(true);
        when(mailInscriptionService.createMail(mailRequest.getBusinessId())).thenReturn(mailDto);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);

        // WHEN
        mailRequestProcessor.processMailRequest(mailRequest);

        // THEN — le mail est envoyé et le statut passe à SENT
        verify(emailSender, times(1)).send(mimeMessage);
        ArgumentCaptor<MailRequestEntity> captor = ArgumentCaptor.forClass(MailRequestEntity.class);
        verify(mailRequestRepository, times(1)).save(captor.capture());
        assertEquals(MailRequestStatut.SENT, captor.getValue().getStatut());
    }

    // -----------------------------------------------------------------------
    // processMailRequest — exception lors de la création du MailDto → mail passe en ERROR
    // -----------------------------------------------------------------------

    @Test
    public void testProcessMailRequestSetsStatusErrorOnException() {
        // GIVEN — le service de mail lève une exception (ex: données manquantes)
        MailRequestEntity mailRequest = buildMailRequest(7L, MailRequestType.INSCRIPTION, Collections.emptyList());
        when(paramService.isSendEmailEnabled()).thenReturn(true);
        when(mailInscriptionService.createMail(anyLong()))
                .thenThrow(new RuntimeException("Erreur service mail"));

        // WHEN
        mailRequestProcessor.processMailRequest(mailRequest);

        // THEN — le statut passe en ERROR et le record est sauvegardé
        ArgumentCaptor<MailRequestEntity> captor = ArgumentCaptor.forClass(MailRequestEntity.class);
        verify(mailRequestRepository, times(1)).save(captor.capture());
        assertEquals(MailRequestStatut.ERROR, captor.getValue().getStatut());
    }

    // -----------------------------------------------------------------------
    // enrichWithGeneratedDocuments — aucun document lié → MailDto inchangé,
    //                                aucune requête sur DocumentRequestRepository
    // -----------------------------------------------------------------------

    @Test
    public void testEnrichWithGeneratedDocumentsWhenNoLinkedDocuments() {
        // GIVEN — mail sans documents liés
        MailRequestEntity mailRequest = buildMailRequest(1L, MailRequestType.INSCRIPTION, Collections.emptyList());
        MailDto mailDto = buildMailDto("Confirmation inscription");

        // WHEN — appel direct de la méthode privée via ReflectionTestUtils
        ReflectionTestUtils.invokeMethod(mailRequestProcessor, "enrichWithGeneratedDocuments", mailRequest, mailDto);

        // THEN — DocumentRequestRepository n'est jamais consulté et le mailDto n'a pas de pièces jointes générées
        verify(documentRequestRepository, never()).findAllById(any());
        assertNull(mailDto.attachments(), "Aucune pièce jointe ne doit être ajoutée si aucun document lié");
    }

    // -----------------------------------------------------------------------
    // enrichWithGeneratedDocuments — documents liés tous COMPLETED avec documentPath
    //                                → tous ajoutés en pièces jointes dans le MailDto
    // -----------------------------------------------------------------------

    @Test
    public void testEnrichWithGeneratedDocumentsWhenAllDocumentsCompleted() {
        // GIVEN
        Long docId1 = 10L;
        Long docId2 = 11L;

        DocumentRequestEntity doc1 = buildDocumentRequest(docId1, DocumentRequestStatut.COMPLETED, "/docs/bulletin-10.pdf");
        DocumentRequestEntity doc2 = buildDocumentRequest(docId2, DocumentRequestStatut.COMPLETED, "/docs/inscription-11.pdf");

        List<MailRequestDocumentRequestEntity> links = List.of(
                MailRequestDocumentRequestEntity.builder().mailRequestId(1L).documentRequestId(docId1).build(),
                MailRequestDocumentRequestEntity.builder().mailRequestId(1L).documentRequestId(docId2).build()
        );
        MailRequestEntity mailRequest = buildMailRequest(1L, MailRequestType.INSCRIPTION, links);
        MailDto mailDto = buildMailDto("Confirmation inscription");

        when(documentRequestRepository.findAllById(List.of(docId1, docId2)))
                .thenReturn(List.of(doc1, doc2));

        // WHEN
        ReflectionTestUtils.invokeMethod(mailRequestProcessor, "enrichWithGeneratedDocuments", mailRequest, mailDto);

        // THEN — les deux pièces jointes générées sont présentes dans le MailDto
        verify(documentRequestRepository, times(1)).findAllById(List.of(docId1, docId2));
        assertNotNull(mailDto.attachments());
        assertEquals(2, mailDto.attachments().size());
        assertTrue(mailDto.attachments().stream().anyMatch(a -> a.getLocation().equals("/docs/bulletin-10.pdf")));
        assertTrue(mailDto.attachments().stream().anyMatch(a -> a.getLocation().equals("/docs/inscription-11.pdf")));
        assertTrue(mailDto.attachments().stream().anyMatch(a -> a.getName().equals("bulletin-10.pdf")));
        assertTrue(mailDto.attachments().stream().anyMatch(a -> a.getName().equals("inscription-11.pdf")));
    }

    // -----------------------------------------------------------------------
    // enrichWithGeneratedDocuments — document lié en ERROR
    //                                → PendingDocumentGenerationException levée
    // -----------------------------------------------------------------------

    @Test
    public void testEnrichWithGeneratedDocumentsWhenDocumentInError() {
        // GIVEN — un document en ERROR (non-COMPLETED)
        Long docId = 20L;
        DocumentRequestEntity docEnError = buildDocumentRequest(docId, DocumentRequestStatut.ERROR, null);

        List<MailRequestDocumentRequestEntity> links = List.of(
                MailRequestDocumentRequestEntity.builder().mailRequestId(2L).documentRequestId(docId).build()
        );
        MailRequestEntity mailRequest = buildMailRequest(2L, MailRequestType.INSCRIPTION, links);
        MailDto mailDto = buildMailDto("Confirmation inscription");

        when(documentRequestRepository.findAllById(List.of(docId)))
                .thenReturn(List.of(docEnError));

        // WHEN + THEN — l'exception PendingDocumentGenerationException est levée
        assertThrows(PendingDocumentGenerationException.class,
                () -> ReflectionTestUtils.invokeMethod(mailRequestProcessor, "enrichWithGeneratedDocuments",
                        mailRequest, mailDto));
    }

    // -----------------------------------------------------------------------
    // enrichWithGeneratedDocuments — document lié PENDING (pas encore traité)
    //                                → PendingDocumentGenerationException levée
    // -----------------------------------------------------------------------

    @Test
    public void testEnrichWithGeneratedDocumentsWhenDocumentStillPending() {
        // GIVEN — un document encore en PENDING
        Long docId = 30L;
        DocumentRequestEntity docPending = buildDocumentRequest(docId, DocumentRequestStatut.PENDING, null);

        List<MailRequestDocumentRequestEntity> links = List.of(
                MailRequestDocumentRequestEntity.builder().mailRequestId(3L).documentRequestId(docId).build()
        );
        MailRequestEntity mailRequest = buildMailRequest(3L, MailRequestType.INSCRIPTION, links);
        MailDto mailDto = buildMailDto("Confirmation inscription");

        when(documentRequestRepository.findAllById(List.of(docId)))
                .thenReturn(List.of(docPending));

        // WHEN + THEN — l'exception est levée car le document n'est pas encore COMPLETED
        assertThrows(PendingDocumentGenerationException.class,
                () -> ReflectionTestUtils.invokeMethod(mailRequestProcessor, "enrichWithGeneratedDocuments",
                        mailRequest, mailDto));
    }

    // -----------------------------------------------------------------------
    // enrichWithGeneratedDocuments — pièces jointes existantes conservées
    //                                (ex : RIB statique déjà dans le MailDto)
    // -----------------------------------------------------------------------

    @Test
    public void testEnrichWithGeneratedDocumentsPreservesExistingAttachments() {
        // GIVEN — MailDto avec un RIB statique déjà en pièce jointe
        Long docId = 40L;
        DocumentRequestEntity doc = buildDocumentRequest(docId, DocumentRequestStatut.COMPLETED, "/docs/bulletin-40.pdf");

        List<MailRequestDocumentRequestEntity> links = List.of(
                MailRequestDocumentRequestEntity.builder().mailRequestId(4L).documentRequestId(docId).build()
        );
        MailRequestEntity mailRequest = buildMailRequest(4L, MailRequestType.ADHESION, links);

        MailAttachmentDto ribStatique = MailAttachmentDto.builder()
                .name("rib-amc.pdf")
                .location("/static/rib-amc.pdf")
                .build();
        MailDto mailDto = buildMailDtoWithAttachment(ribStatique);

        when(documentRequestRepository.findAllById(List.of(docId)))
                .thenReturn(List.of(doc));

        // WHEN
        ReflectionTestUtils.invokeMethod(mailRequestProcessor, "enrichWithGeneratedDocuments", mailRequest, mailDto);

        // THEN — le RIB statique ET le document généré sont présents dans les pièces jointes
        assertNotNull(mailDto.attachments());
        assertEquals(2, mailDto.attachments().size());
        assertTrue(mailDto.attachments().stream().anyMatch(a -> a.getLocation().equals("/static/rib-amc.pdf")));
        assertTrue(mailDto.attachments().stream().anyMatch(a -> a.getLocation().equals("/docs/bulletin-40.pdf")));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MailRequestEntity buildMailRequest(Long id, MailRequestType type,
                                               List<MailRequestDocumentRequestEntity> documentLinks) {
        MailRequestEntity entity = MailRequestEntity.builder()
                .type(type)
                .businessId(100L)
                .statut(MailRequestStatut.PENDING)
                .build();
        entity.setId(id);
        entity.getDocumentRequests().addAll(documentLinks);
        return entity;
    }

    private DocumentRequestEntity buildDocumentRequest(Long id, DocumentRequestStatut statut, String documentPath) {
        DocumentRequestEntity doc = new DocumentRequestEntity();
        doc.setId(id);
        doc.setType(DocumentRequestType.BULLETIN);
        doc.setStatut(statut);
        doc.setDocumentPath(documentPath);
        return doc;
    }

    /**
     * Construit un MailDto sans pièces jointes.
     * Lombok @Singular produit une liste immuable lors du build(). Pour permettre à
     * addAttachments() d'y ajouter des éléments, on force le champ à null via ReflectionTestUtils
     * afin que la méthode crée sa propre ArrayList interne.
     */
    private MailDto buildMailDto(String subject) {
        MailDto dto = new MailDto()
                .recipientEmail("destinataire@test.com")
                .subject(subject)
                .body("<p>Corps du mail</p>");
        ReflectionTestUtils.setField(dto, "attachments", null);
        return dto;
    }

    /**
     * Construit un MailDto avec une pièce jointe initiale dans une liste modifiable,
     * pour vérifier que addAttachments() ajoute les nouveaux éléments sans écraser l'existant.
     */
    private MailDto buildMailDtoWithAttachment(MailAttachmentDto existingAttachment) {
        MailDto dto = new MailDto()
                .recipientEmail("destinataire@test.com")
                .subject("Sujet test")
                .body("<p>Corps</p>");
        ReflectionTestUtils.setField(dto, "attachments", new ArrayList<>(List.of(existingAttachment)));
        return dto;
    }
}
