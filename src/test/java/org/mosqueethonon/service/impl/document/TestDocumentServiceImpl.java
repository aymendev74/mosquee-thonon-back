package org.mosqueethonon.service.impl.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.service.document.DocumentGenerator;
import org.mosqueethonon.service.document.PdfGeneratorService;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestDocumentServiceImpl {

    @TempDir
    private Path tempDir;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    private ApplicationConfiguration applicationConfiguration;

    private DocumentServiceImpl documentService;

    @BeforeEach
    public void setUp() {
        ApplicationConfiguration.Documents documents = new ApplicationConfiguration.Documents();
        documents.setBasePath(tempDir.toString());
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setDocuments(documents);

        documentService = new DocumentServiceImpl(documentRepository, pdfGeneratorService, applicationConfiguration);
    }

    // -----------------------------------------------------------------------
    // generateOrUpdateDocument — pas de document existant : création
    // -----------------------------------------------------------------------

    @Test
    public void testGenerateOrUpdateDocumentCreeUnNouveauDocumentQuandAucunExistant() {
        // GIVEN
        FakeDocumentGenerator<Object> generator = FakeDocumentGenerator.builder()
                .code("TYPE-001")
                .path("TYPE")
                .templateName("documents/type-001")
                .fileName("fichier.pdf")
                .annee("2025")
                .idUtilisateur(9L)
                .hash("hash1")
                .templateVariables(Map.of("cle", "valeur"))
                .metadata(List.of(new DocumentMetadataEntity(DocumentMetadataKey.NOM, "Dupont")))
                .build();
        Object entity = new Object();
        byte[] pdfContent = "PDF-CONTENT".getBytes(StandardCharsets.UTF_8);

        when(documentRepository.findByChemin("TYPE/2025/fichier.pdf")).thenReturn(Optional.empty());
        when(pdfGeneratorService.generatePdf("documents/type-001", Map.of("cle", "valeur"))).thenReturn(pdfContent);
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        DocumentEntity result = documentService.generateOrUpdateDocument(generator, entity);

        // THEN
        assertEquals("fichier.pdf", result.getLibelle());
        assertEquals("TYPE-001", result.getCode());
        assertEquals("TYPE/2025/fichier.pdf", result.getChemin());
        assertEquals("hash1", result.getHash());
        assertEquals("2025", result.getAnnee());
        assertEquals(9L, result.getIdUtilisateur());
        assertEquals(1, result.getMetadonnees().size());
        assertEquals("Dupont", result.getMetadonnees().get(0).getValeur());

        Path expectedFile = tempDir.resolve("TYPE").resolve("2025").resolve("fichier.pdf");
        assertTrue(Files.exists(expectedFile));
        assertArrayEquals(pdfContent, readFile(expectedFile));

        verify(documentRepository).save(any(DocumentEntity.class));
    }

    @Test
    public void testGenerateOrUpdateDocumentSansAnneeOmetLeSegmentAnnee() {
        // GIVEN
        FakeDocumentGenerator<Object> generator = FakeDocumentGenerator.builder()
                .code("TYPE-001")
                .path("TYPE")
                .templateName("documents/type-001")
                .fileName("fichier.pdf")
                .annee(null)
                .idUtilisateur(null)
                .hash("hash1")
                .templateVariables(Map.of())
                .metadata(List.of())
                .build();
        Object entity = new Object();

        when(documentRepository.findByChemin("TYPE/fichier.pdf")).thenReturn(Optional.empty());
        when(pdfGeneratorService.generatePdf(anyString(), anyMap())).thenReturn("content".getBytes(StandardCharsets.UTF_8));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        DocumentEntity result = documentService.generateOrUpdateDocument(generator, entity);

        // THEN
        assertEquals("TYPE/fichier.pdf", result.getChemin());
        assertTrue(Files.exists(tempDir.resolve("TYPE").resolve("fichier.pdf")));
    }

    @Test
    public void testGenerateOrUpdateDocumentAvecAnneeBlancheOmetLeSegmentAnnee() {
        // GIVEN
        FakeDocumentGenerator<Object> generator = FakeDocumentGenerator.builder()
                .code("TYPE-001")
                .path("TYPE")
                .templateName("documents/type-001")
                .fileName("fichier.pdf")
                .annee("   ")
                .idUtilisateur(null)
                .hash("hash1")
                .templateVariables(Map.of())
                .metadata(List.of())
                .build();
        Object entity = new Object();

        when(documentRepository.findByChemin("TYPE/fichier.pdf")).thenReturn(Optional.empty());
        when(pdfGeneratorService.generatePdf(anyString(), anyMap())).thenReturn("content".getBytes(StandardCharsets.UTF_8));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        DocumentEntity result = documentService.generateOrUpdateDocument(generator, entity);

        // THEN
        assertEquals("TYPE/fichier.pdf", result.getChemin());
    }

    // -----------------------------------------------------------------------
    // generateOrUpdateDocument — document existant, hash identique
    // -----------------------------------------------------------------------

    @Test
    public void testGenerateOrUpdateDocumentRetourneDocumentExistantQuandHashIdentique() {
        // GIVEN
        FakeDocumentGenerator<Object> generator = FakeDocumentGenerator.builder()
                .code("TYPE-001")
                .path("TYPE")
                .templateName("documents/type-001")
                .fileName("fichier.pdf")
                .annee("2025")
                .idUtilisateur(9L)
                .hash("hash-inchange")
                .templateVariables(Map.of())
                .metadata(List.of())
                .build();
        Object entity = new Object();

        DocumentEntity existingDoc = new DocumentEntity();
        existingDoc.setChemin("TYPE/2025/fichier.pdf");
        existingDoc.setHash("hash-inchange");

        when(documentRepository.findByChemin("TYPE/2025/fichier.pdf")).thenReturn(Optional.of(existingDoc));

        // WHEN
        DocumentEntity result = documentService.generateOrUpdateDocument(generator, entity);

        // THEN
        assertSame(existingDoc, result);
        verifyNoInteractions(pdfGeneratorService);
        verify(documentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // generateOrUpdateDocument — document existant, hash different : regeneration
    // -----------------------------------------------------------------------

    @Test
    public void testGenerateOrUpdateDocumentRegenereQuandHashDifferent() {
        // GIVEN
        FakeDocumentGenerator<Object> generator = FakeDocumentGenerator.builder()
                .code("TYPE-001")
                .path("TYPE")
                .templateName("documents/type-001")
                .fileName("fichier.pdf")
                .annee("2025")
                .idUtilisateur(9L)
                .hash("hash-nouveau")
                .templateVariables(Map.of("cle", "valeur"))
                .metadata(List.of(new DocumentMetadataEntity(DocumentMetadataKey.NOM, "NouveauNom")))
                .build();
        Object entity = new Object();
        byte[] newContent = "NOUVEAU-CONTENU".getBytes(StandardCharsets.UTF_8);

        DocumentEntity existingDoc = new DocumentEntity();
        existingDoc.setChemin("TYPE/2025/fichier.pdf");
        existingDoc.setHash("hash-ancien");
        existingDoc.getMetadonnees().add(new DocumentMetadataEntity(DocumentMetadataKey.NOM, "AncienNom"));

        when(documentRepository.findByChemin("TYPE/2025/fichier.pdf")).thenReturn(Optional.of(existingDoc));
        when(pdfGeneratorService.generatePdf("documents/type-001", Map.of("cle", "valeur"))).thenReturn(newContent);
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        DocumentEntity result = documentService.generateOrUpdateDocument(generator, entity);

        // THEN
        assertSame(existingDoc, result);
        assertEquals("hash-nouveau", result.getHash());
        assertEquals(1, result.getMetadonnees().size());
        assertEquals("NouveauNom", result.getMetadonnees().get(0).getValeur());

        Path expectedFile = tempDir.resolve("TYPE").resolve("2025").resolve("fichier.pdf");
        assertTrue(Files.exists(expectedFile));
        assertArrayEquals(newContent, readFile(expectedFile));

        verify(documentRepository).save(existingDoc);
    }

    // -----------------------------------------------------------------------
    // getDocumentContent
    // -----------------------------------------------------------------------

    @Test
    public void testGetDocumentContentLitLesBytesReelsDuFichier() throws IOException {
        // GIVEN
        DocumentEntity doc = new DocumentEntity();
        doc.setId(1L);
        doc.setChemin("TYPE/2025/fichier.pdf");
        byte[] content = "CONTENU-REEL".getBytes(StandardCharsets.UTF_8);
        Path filePath = tempDir.resolve("TYPE").resolve("2025").resolve("fichier.pdf");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        // WHEN
        byte[] result = documentService.getDocumentContent(1L);

        // THEN
        assertArrayEquals(content, result);
    }

    // -----------------------------------------------------------------------
    // findById
    // -----------------------------------------------------------------------

    @Test
    public void testFindByIdRetourneLeDocumentQuandTrouve() {
        // GIVEN
        DocumentEntity doc = new DocumentEntity();
        doc.setId(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        // WHEN
        DocumentEntity result = documentService.findById(1L);

        // THEN
        assertSame(doc, result);
    }

    @Test
    public void testFindByIdLeveResourceNotFoundExceptionQuandAbsent() {
        // GIVEN
        when(documentRepository.findById(404L)).thenReturn(Optional.empty());

        // WHEN THEN
        assertThrows(ResourceNotFoundException.class, () -> documentService.findById(404L));
    }

    // -----------------------------------------------------------------------
    // deleteDocument
    // -----------------------------------------------------------------------

    @Test
    public void testDeleteDocumentNoOpQuandDocumentIntrouvable() {
        // GIVEN
        when(documentRepository.findById(404L)).thenReturn(Optional.empty());

        // WHEN
        documentService.deleteDocument(404L);

        // THEN
        verify(documentRepository, never()).delete(any());
    }

    @Test
    public void testDeleteDocumentSupprimeEnBaseEtPlanifieLaSuppressionPhysique() throws IOException {
        // GIVEN
        DocumentEntity doc = new DocumentEntity();
        doc.setId(1L);
        doc.setChemin("TYPE/2025/fichier.pdf");
        Path filePath = tempDir.resolve("TYPE").resolve("2025").resolve("fichier.pdf");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "contenu".getBytes(StandardCharsets.UTF_8));

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        // WHEN — on simule une transaction réelle pour pouvoir déclencher le afterCommit()
        TransactionSynchronizationManager.initSynchronization();
        try {
            documentService.deleteDocument(1L);

            // THEN — la suppression en base a bien lieu immédiatement
            verify(documentRepository).delete(doc);
            assertTrue(Files.exists(filePath), "Le fichier physique ne doit pas être supprimé avant le commit");

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());

            // Simule le commit de la transaction
            for (TransactionSynchronization synchronization : synchronizations) {
                synchronization.afterCommit();
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        // THEN — le fichier physique est supprimé après le commit
        assertFalse(Files.exists(filePath));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private byte[] readFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fake / stub de DocumentGenerator&lt;T&gt; entièrement configurable, utilisé pour tester
     * DocumentServiceImpl sans se coupler à une implémentation concrète de générateur.
     */
    private static class FakeDocumentGenerator<T> implements DocumentGenerator<T> {

        private String code;
        private String path;
        private String templateName;
        private String fileName;
        private String annee;
        private Long idUtilisateur;
        private String hash;
        private Map<String, Object> templateVariables = new HashMap<>();
        private List<DocumentMetadataEntity> metadata = new ArrayList<>();

        static <T> Builder<T> builder() {
            return new Builder<>();
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getTemplateName() {
            return templateName;
        }

        @Override
        public String generateFileName(T entity) {
            return fileName;
        }

        @Override
        public String getAnnee(T entity) {
            return annee;
        }

        @Override
        public Long getIdUtilisateur(T entity) {
            return idUtilisateur;
        }

        @Override
        public Map<String, Object> buildTemplateVariables(T entity) {
            return templateVariables;
        }

        @Override
        public String computeHash(T entity) {
            return hash;
        }

        @Override
        public List<DocumentMetadataEntity> buildMetadata(T entity) {
            return metadata;
        }

        static class Builder<T> {
            private final FakeDocumentGenerator<T> instance = new FakeDocumentGenerator<>();

            Builder<T> code(String code) {
                instance.code = code;
                return this;
            }

            Builder<T> path(String path) {
                instance.path = path;
                return this;
            }

            Builder<T> templateName(String templateName) {
                instance.templateName = templateName;
                return this;
            }

            Builder<T> fileName(String fileName) {
                instance.fileName = fileName;
                return this;
            }

            Builder<T> annee(String annee) {
                instance.annee = annee;
                return this;
            }

            Builder<T> idUtilisateur(Long idUtilisateur) {
                instance.idUtilisateur = idUtilisateur;
                return this;
            }

            Builder<T> hash(String hash) {
                instance.hash = hash;
                return this;
            }

            Builder<T> templateVariables(Map<String, Object> templateVariables) {
                instance.templateVariables = templateVariables;
                return this;
            }

            Builder<T> metadata(List<DocumentMetadataEntity> metadata) {
                instance.metadata = new ArrayList<>(metadata);
                return this;
            }

            FakeDocumentGenerator<T> build() {
                return instance;
            }
        }
    }

}
