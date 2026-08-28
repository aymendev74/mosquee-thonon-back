package org.mosqueethonon.chatbot.scheduled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.chatbot.ChatbotTestProperties;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.service.ChatbotIndexingService;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class TestChatbotIndexingJob {

    private ChatbotIndexingService chatbotIndexingService;

    private ChatbotProperties properties;

    private ChatbotIndexingJob underTest;

    @BeforeEach
    void setUp() {
        this.chatbotIndexingService = mock(ChatbotIndexingService.class);
        this.properties = ChatbotTestProperties.build();
        this.underTest = new ChatbotIndexingJob(this.chatbotIndexingService, this.properties);
    }

    @Test
    void testReindexesWhenEnabled() {
        when(this.chatbotIndexingService.reindexIfOutdated()).thenReturn(OptionalInt.of(71));

        this.underTest.reindexIfOutdated();

        verify(this.chatbotIndexingService).reindexIfOutdated();
    }

    /**
     * Garde-fou du profil de test : le job démarre avec le contexte Spring, et sans cet interrupteur
     * chaque exécution de la suite appellerait réellement l'API Gemini.
     */
    @Test
    void testDoesNothingWhenDisabled() {
        this.properties.getIndexing().setEnabled(Boolean.FALSE);

        this.underTest.reindexIfOutdated();

        verifyNoInteractions(this.chatbotIndexingService);
    }

    /**
     * Une exception qui remonterait au scheduler annulerait définitivement les cycles suivants : une
     * API Gemini indisponible doit rester rattrapable au cycle d'après.
     */
    @Test
    void testSwallowsIndexingFailure() {
        when(this.chatbotIndexingService.reindexIfOutdated())
                .thenThrow(new IllegalStateException("API Gemini indisponible"));

        assertDoesNotThrow(() -> this.underTest.reindexIfOutdated());
    }

}
