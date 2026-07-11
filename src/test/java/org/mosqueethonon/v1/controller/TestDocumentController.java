package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.document.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestDocumentController {

    // -----------------------------------------------------------------------
    // Constantes
    // -----------------------------------------------------------------------

    private static final Long DOCUMENT_ID = 42L;
    private static final Long OWNER_USER_ID = 10L;
    private static final Long OTHER_USER_ID = 99L;
    private static final String OWNER_USERNAME = "utilisateur.proprio";
    private static final String OTHER_USERNAME = "autre.utilisateur";
    private static final byte[] FAKE_PDF_CONTENT = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF

    // -----------------------------------------------------------------------
    // Mocks & SUT
    // -----------------------------------------------------------------------

    @Mock
    private DocumentService documentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private DocumentController documentController;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Construit un DocumentEntity avec le code et l'idUtilisateur donnés.
     */
    private DocumentEntity buildDocument(String code, Long idUtilisateur) {
        DocumentEntity doc = new DocumentEntity();
        doc.setId(DOCUMENT_ID);
        doc.setCode(code);
        doc.setLibelle("document-test.pdf");
        doc.setIdUtilisateur(idUtilisateur);
        return doc;
    }

    /**
     * Construit un UtilisateurEntity avec l'identifiant donné.
     */
    private UtilisateurEntity buildUser(Long id) {
        UtilisateurEntity user = new UtilisateurEntity();
        user.setId(id);
        return user;
    }

    /**
     * Configure les mocks pour simuler un utilisateur authentifié NON propriétaire du document.
     */
    private void givenAuthenticatedNonOwner(String username) {
        when(securityContext.getUser()).thenReturn(username);
        UtilisateurEntity nonOwner = buildUser(OTHER_USER_ID);
        when(utilisateurRepository.findByUsername(username))
                .thenReturn(Optional.of(nonOwner));
    }

    /**
     * Configure les mocks pour simuler l'utilisateur propriétaire du document.
     */
    private void givenOwnerAuthenticated() {
        when(securityContext.getUser()).thenReturn(OWNER_USERNAME);
        UtilisateurEntity owner = buildUser(OWNER_USER_ID);
        when(utilisateurRepository.findByUsername(OWNER_USERNAME))
                .thenReturn(Optional.of(owner));
    }

    // -----------------------------------------------------------------------
    // Groupe 1 : Propriétaire du document — le return anticipé fonctionne
    // -----------------------------------------------------------------------

    @Nested
    class QuandUtilisateurEstProprietaire {

        /**
         * Le propriétaire d'un document ADHESION-001 peut y accéder sans contrôle de rôle.
         * Le `return` ligne 47 court-circuite toute la logique de rôles ET le throw inconditionnel.
         */
        @Test
        void proprietairePeutAccederDocumentAdhesion() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * Le propriétaire d'un document INS-ADULTE-001 peut y accéder.
         */
        @Test
        void proprietairePeutAccederDocumentInscriptionAdulte() {
            DocumentEntity doc = buildDocument("INS-ADULTE-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Le propriétaire d'un document INS-ENFANT-001 peut y accéder.
         */
        @Test
        void proprietairePeutAccederDocumentInscriptionEnfant() {
            DocumentEntity doc = buildDocument("INS-ENFANT-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Le propriétaire d'un document BULLETIN-001 peut y accéder.
         */
        @Test
        void proprietairePeutAccederDocumentBulletin() {
            DocumentEntity doc = buildDocument("BULLETIN-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Le propriétaire d'un document de type inconnu peut quand même y accéder
         * grâce au return anticipé — il n'atteint jamais le throw inconditionnel.
         */
        @Test
        void proprietairePeutAccederDocumentTypeInconnu() {
            DocumentEntity doc = buildDocument("TYPE-INCONNU-XYZ", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Quand le propriétaire accède, les services getDocumentContent et findById
         * sont tous les deux appelés exactement une fois.
         */
        @Test
        void serviceFindByIdEtGetDocumentContentAppelesPourProprietaire() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            documentController.getDocumentContent(DOCUMENT_ID);

            verify(documentService, times(1)).findById(DOCUMENT_ID);
            verify(documentService, times(1)).getDocumentContent(DOCUMENT_ID);
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 2 : Document ADHESION-001 — non-propriétaires
    // -----------------------------------------------------------------------

    @Nested
    class QuandDocumentEstAdhesion {

        private DocumentEntity docAdhesion;

        @BeforeEach
        void setUp() {
            docAdhesion = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docAdhesion);
        }

        /**
         * Le TRESORIER (non propriétaire) peut accéder à ADHESION-001.
         */
        @Test
        void tresorierNonProprietairePeutAccederAdhesion() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            // hasRole("ROLE_TRESORIER") = true => court-circuit && => hasRole("ROLE_ADMIN") non évalué
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * L'ADMIN (non propriétaire) peut accéder à ADHESION-001.
         */
        @Test
        void adminNonProprietairePeutAccederAdhesion() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * L'ENSEIGNANT (non propriétaire) ne doit PAS pouvoir accéder à ADHESION-001.
         * La vérification de rôle interne lève FORBIDDEN avant même le throw inconditionnel.
         */
        @Test
        void enseignantNePeutPasAccederAdhesion() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        /**
         * Un utilisateur sans rôle privilégié ne doit PAS pouvoir accéder à ADHESION-001.
         */
        @Test
        void utilisateurStandardNePeutPasAccederAdhesion() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 3 : Document INS-ADULTE-001 — non-propriétaires
    // -----------------------------------------------------------------------

    @Nested
    class QuandDocumentEstInscriptionAdulte {

        private DocumentEntity docInsAdulte;

        @BeforeEach
        void setUp() {
            docInsAdulte = buildDocument("INS-ADULTE-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docInsAdulte);
        }

        /**
         * L'ENSEIGNANT (non propriétaire) peut accéder à INS-ADULTE-001.
         */
        @Test
        void enseignantNonProprietairePeutAccederInscriptionAdulte() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            // hasRole("ROLE_ENSEIGNANT") = true => court-circuit && => hasRole("ROLE_ADMIN") non évalué
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * L'ADMIN (non propriétaire) peut accéder à INS-ADULTE-001.
         */
        @Test
        void adminNonProprietairePeutAccederInscriptionAdulte() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * Le TRESORIER (non propriétaire) ne doit PAS pouvoir accéder à INS-ADULTE-001.
         */
        @Test
        void tresorierNePeutPasAccederInscriptionAdulte() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 4 : Document INS-ENFANT-001 — non-propriétaires
    // -----------------------------------------------------------------------

    @Nested
    class QuandDocumentEstInscriptionEnfant {

        private DocumentEntity docInsEnfant;

        @BeforeEach
        void setUp() {
            docInsEnfant = buildDocument("INS-ENFANT-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docInsEnfant);
        }

        /**
         * L'ENSEIGNANT (non propriétaire) peut accéder à INS-ENFANT-001.
         */
        @Test
        void enseignantNonProprietairePeutAccederInscriptionEnfant() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            // hasRole("ROLE_ENSEIGNANT") = true => court-circuit && => hasRole("ROLE_ADMIN") non évalué
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * L'ADMIN (non propriétaire) peut accéder à INS-ENFANT-001.
         */
        @Test
        void adminNonProprietairePeutAccederInscriptionEnfant() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * Le TRESORIER (non propriétaire) ne doit PAS pouvoir accéder à INS-ENFANT-001.
         */
        @Test
        void tresorierNePeutPasAccederInscriptionEnfant() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 5 : Document BULLETIN-001 — non-propriétaires
    // -----------------------------------------------------------------------

    @Nested
    class QuandDocumentEstBulletin {

        private DocumentEntity docBulletin;

        @BeforeEach
        void setUp() {
            docBulletin = buildDocument("BULLETIN-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docBulletin);
        }

        /**
         * L'ENSEIGNANT (non propriétaire) peut accéder à BULLETIN-001.
         */
        @Test
        void enseignantNonProprietairePeutAccederBulletin() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            // hasRole("ROLE_ENSEIGNANT") = true => court-circuit && => hasRole("ROLE_ADMIN") non évalué
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * L'ADMIN (non propriétaire) peut accéder à BULLETIN-001.
         */
        @Test
        void adminNonProprietairePeutAccederBulletin() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(FAKE_PDF_CONTENT, response.getBody());
        }

        /**
         * Le TRESORIER (non propriétaire) ne doit PAS pouvoir accéder à BULLETIN-001.
         */
        @Test
        void tresorierNePeutPasAccederBulletin() {
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 6 : Type de document inconnu ou null
    // -----------------------------------------------------------------------

    @Nested
    class QuandDocumentEstDeTypeInconnu {

        /**
         * Un type de document non reconnu doit toujours aboutir à un refus d'accès
         * pour un non-propriétaire, quel que soit le rôle.
         */
        @Test
        void typeInconnuAccesRefuse() {
            DocumentEntity docInconnu = buildDocument("TYPE-INCONNU-XYZ", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docInconnu);
            givenAuthenticatedNonOwner(OTHER_USERNAME);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        /**
         * Un code null est traité comme type inconnu → refus d'accès pour non-propriétaire.
         */
        @Test
        void codeNullAccesRefuse() {
            DocumentEntity docSansCode = buildDocument(null, OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docSansCode);
            givenAuthenticatedNonOwner(OTHER_USERNAME);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        /**
         * Un code vide est traité comme type inconnu → refus d'accès pour non-propriétaire.
         */
        @Test
        void codeVideAccesRefuse() {
            DocumentEntity docCodeVide = buildDocument("", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(docCodeVide);
            givenAuthenticatedNonOwner(OTHER_USERNAME);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 7 : Utilisateur non authentifié (username null)
    // -----------------------------------------------------------------------

    @Nested
    class QuandUtilisateurNonAuthentifie {

        /**
         * Sans authentification, l'accès à ADHESION-001 est refusé.
         */
        @Test
        void utilisateurNonAuthentifieNePeutPasAccederAdhesion() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            when(securityContext.getUser()).thenReturn(null);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        /**
         * Sans authentification, l'accès à INS-ADULTE-001 est refusé.
         */
        @Test
        void utilisateurNonAuthentifieNePeutPasAccederInscriptionAdulte() {
            DocumentEntity doc = buildDocument("INS-ADULTE-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            when(securityContext.getUser()).thenReturn(null);
            when(securityContext.hasRole("ROLE_ENSEIGNANT")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        /**
         * Sans authentification, l'accès à un type inconnu est refusé.
         */
        @Test
        void utilisateurNonAuthentifieNePeutPasAccederTypeInconnu() {
            DocumentEntity doc = buildDocument("TYPE-INCONNU-XYZ", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            when(securityContext.getUser()).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        /**
         * Quand getUser() retourne null, le repository ne doit pas être interrogé.
         */
        @Test
        void utilisateurNonAuthentifieNInterrogePasLeRepository() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            when(securityContext.getUser()).thenReturn(null);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));

            verify(utilisateurRepository, never()).findByUsername(anyString());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 8 : Utilisateur authentifié mais absent du repository
    // -----------------------------------------------------------------------

    @Nested
    class QuandUtilisateurAuthentifieNonTrouveDansRepository {

        /**
         * Si le username est présent mais inconnu du repository (Optional.empty),
         * l'identifiant courant est null et la comparaison avec idUtilisateur échoue.
         * La vérification de rôle s'applique alors normalement.
         */
        @Test
        void usernameInconnuDuRepositoryAppliqueVerificationDeRole() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            when(securityContext.getUser()).thenReturn(OTHER_USERNAME);
            when(utilisateurRepository.findByUsername(OTHER_USERNAME)).thenReturn(Optional.empty());
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    // -----------------------------------------------------------------------
    // Groupe 9 : Interactions avec les services
    // -----------------------------------------------------------------------

    @Nested
    class InteractionsAvecLesServices {

        /**
         * Quand l'accès est refusé, getDocumentContent du service NE DOIT PAS être appelé.
         */
        @Test
        void serviceGetDocumentContentNonAppeleQuandAccesRefuse() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));

            verify(documentService, never()).getDocumentContent(anyLong());
        }

        /**
         * La méthode findById du service est toujours appelée, même si l'accès sera refusé.
         */
        @Test
        void serviceFindByIdEstToujoursAppeleAvantLeControleAcces() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));

            verify(documentService, times(1)).findById(DOCUMENT_ID);
        }

        /**
         * Le message de l'exception FORBIDDEN correspond au texte défini dans le contrôleur.
         */
        @Test
        void messageExceptionForbiddenEstCorrect() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenAuthenticatedNonOwner(OTHER_USERNAME);
            when(securityContext.hasRole("ROLE_TRESORIER")).thenReturn(false);
            when(securityContext.hasRole("ROLE_ADMIN")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> documentController.getDocumentContent(DOCUMENT_ID));

            assertTrue(ex.getReason().contains("Accès refusé"),
                    "Le message doit contenir 'Accès refusé', obtenu : " + ex.getReason());
        }

        /**
         * La réponse du propriétaire contient bien le contenu PDF et les bons headers.
         */
        @Test
        void reponseProprietaireContientLeContenuPdf() {
            DocumentEntity doc = buildDocument("ADHESION-001", OWNER_USER_ID);
            when(documentService.findById(DOCUMENT_ID)).thenReturn(doc);
            givenOwnerAuthenticated();
            when(documentService.getDocumentContent(DOCUMENT_ID)).thenReturn(FAKE_PDF_CONTENT);

            ResponseEntity<byte[]> response = documentController.getDocumentContent(DOCUMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(FAKE_PDF_CONTENT.length, response.getHeaders().getContentLength());
        }
    }
}
