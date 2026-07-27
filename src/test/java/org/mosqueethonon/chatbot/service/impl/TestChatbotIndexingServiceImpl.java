package org.mosqueethonon.chatbot.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mosqueethonon.chatbot.ChatbotTestProperties;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.repository.ChatbotDocumentChunkRepository;
import org.mosqueethonon.chatbot.repository.ChatbotIndexStateRepository;
import org.mosqueethonon.chatbot.service.EmbeddingService;
import org.mosqueethonon.common.config.TimeConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Couvre la réindexation conditionnelle : ce qui doit déclencher une réindexation, et surtout ce qui
 * ne doit rien déclencher du tout — chaque réindexation inutile consomme du quota Gemini.
 */
public class TestChatbotIndexingServiceImpl {

    private static final String DOC_CONTENT = """
            # Titre du document

            Chapeau introductif, non indexé.

            ## Première section

            Contenu de la première section.

            ## Deuxième section

            Contenu de la deuxième section.
            """;

    @TempDir
    Path docsDir;

    private EmbeddingService embeddingService;

    private ChatbotDocumentChunkRepository chunkRepository;

    private ChatbotIndexStateRepository indexStateRepository;

    private ChatbotProperties properties;

    private ChatbotIndexingServiceImpl underTest;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(this.docsDir.resolve("adhesions.md"), DOC_CONTENT);

        this.embeddingService = mock(EmbeddingService.class);
        this.chunkRepository = mock(ChatbotDocumentChunkRepository.class);
        this.indexStateRepository = mock(ChatbotIndexStateRepository.class);

        this.properties = ChatbotTestProperties.build();
        this.properties.setDocsPath(this.docsDir.toString());

        this.underTest = new ChatbotIndexingServiceImpl(Clock.system(TimeConfiguration.ZONE_APPLICATION),
                this.embeddingService, this.chunkRepository, this.indexStateRepository, this.properties);

        stubEmbeddings();
    }

    @Test
    void testReindexIfOutdatedIndexesWhenNoSignatureStored() {
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("");

        OptionalInt indexed = this.underTest.reindexIfOutdated();

        assertTrue(indexed.isPresent());
        assertEquals(2, indexed.getAsInt());
        verify(this.chunkRepository).deleteAll();
        verify(this.chunkRepository).insertAll(any());
        verify(this.indexStateRepository).updateSignature(anyString(), eq(2));
    }

    @Test
    void testReindexIfOutdatedDoesNothingWhenSignatureUnchanged() {
        // Première passe : on capture la signature réellement calculée.
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("");
        this.underTest.reindexIfOutdated();
        String signature = captureSignature();

        // Seconde passe : la base porte déjà cette signature, plus rien ne doit se produire.
        resetMocks();
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn(signature);

        OptionalInt indexed = this.underTest.reindexIfOutdated();

        assertTrue(indexed.isEmpty());
        verifyNoInteractions(this.embeddingService);
        verify(this.chunkRepository, never()).deleteAll();
        verify(this.chunkRepository, never()).insertAll(any());
        verify(this.indexStateRepository, never()).updateSignature(anyString(), anyInt());
    }

    @Test
    void testReindexIfOutdatedIndexesWhenDocumentationChanged() throws IOException {
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("");
        this.underTest.reindexIfOutdated();
        String signature = captureSignature();

        Files.writeString(this.docsDir.resolve("adhesions.md"),
                DOC_CONTENT + "\n## Troisième section\n\nUne nouveauté.\n");
        resetMocks();
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn(signature);

        OptionalInt indexed = this.underTest.reindexIfOutdated();

        assertTrue(indexed.isPresent());
        assertEquals(3, indexed.getAsInt());
        assertNotEquals(signature, captureSignature());
    }

    /**
     * Le cas qui justifie d'inclure le modèle dans la signature : la documentation n'a pas bougé d'un
     * octet, mais les vecteurs stockés proviennent d'un autre modèle et sont donc incomparables.
     */
    @Test
    void testReindexIfOutdatedIndexesWhenEmbeddingModelChangedWithIdenticalDocs() {
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("");
        this.underTest.reindexIfOutdated();
        String signature = captureSignature();

        resetMocks();
        this.properties.getGemini().setEmbeddingModel("gemini-embedding-2");
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn(signature);

        OptionalInt indexed = this.underTest.reindexIfOutdated();

        assertTrue(indexed.isPresent());
        verify(this.chunkRepository).insertAll(any());
    }

    @Test
    void testReindexIfOutdatedIndexesWhenEmbeddingDimensionChanged() {
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("");
        this.underTest.reindexIfOutdated();
        String signature = captureSignature();

        resetMocks();
        this.properties.getGemini().setEmbeddingDimension(1536);
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn(signature);

        assertTrue(this.underTest.reindexIfOutdated().isPresent());
    }

    /**
     * L'endpoint admin doit réindexer même quand rien n'a changé, tout en réécrivant la signature :
     * sans cela le démarrage suivant réindexerait une seconde fois pour rien.
     */
    @Test
    void testReindexAlwaysIndexesAndStoresSignature() {
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("");
        this.underTest.reindexIfOutdated();
        String signature = captureSignature();

        resetMocks();
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn(signature);

        int indexed = this.underTest.reindex();

        assertEquals(2, indexed);
        verify(this.chunkRepository).insertAll(any());
        assertEquals(signature, captureSignature());
    }

    @Test
    void testReindexPurgesIndexWhenNoDocumentRemains() throws IOException {
        Files.delete(this.docsDir.resolve("adhesions.md"));
        when(this.indexStateRepository.findSignatureForUpdate()).thenReturn("une-ancienne-signature");

        OptionalInt indexed = this.underTest.reindexIfOutdated();

        assertTrue(indexed.isPresent());
        assertEquals(0, indexed.getAsInt());
        verify(this.chunkRepository).deleteAll();
        verify(this.chunkRepository, never()).insertAll(any());
        verifyNoInteractions(this.embeddingService);
    }

    private String captureSignature() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(this.indexStateRepository).updateSignature(captor.capture(), anyInt());
        return captor.getValue();
    }

    private void resetMocks() {
        reset(this.embeddingService, this.chunkRepository, this.indexStateRepository);
        stubEmbeddings();
    }

    private void stubEmbeddings() {
        when(this.embeddingService.embedBatch(any()))
                .thenAnswer(invocation -> ((List<String>) invocation.getArgument(0)).stream()
                        .map(text -> new float[]{1.0f})
                        .toList());
    }

}
