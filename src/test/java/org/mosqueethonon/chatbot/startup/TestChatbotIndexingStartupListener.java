package org.mosqueethonon.chatbot.startup;

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

public class TestChatbotIndexingStartupListener {

    private ChatbotIndexingService chatbotIndexingService;

    private ChatbotProperties properties;

    private ChatbotIndexingStartupListener underTest;

    @BeforeEach
    void setUp() {
        this.chatbotIndexingService = mock(ChatbotIndexingService.class);
        this.properties = ChatbotTestProperties.build();
        this.underTest = new ChatbotIndexingStartupListener(this.chatbotIndexingService, this.properties);
    }

    @Test
    void testReindexesWhenEnabled() {
        when(this.chatbotIndexingService.reindexIfOutdated()).thenReturn(OptionalInt.of(71));

        this.underTest.reindexIfOutdated();

        verify(this.chatbotIndexingService).reindexIfOutdated();
    }

    /**
     * Garde-fou du profil de test : les @SpringBootTest publient ApplicationReadyEvent, et sans cet
     * interrupteur chaque exécution de la suite appellerait réellement l'API Gemini.
     */
    @Test
    void testDoesNothingWhenDisabled() {
        this.properties.getIndexing().setOnStartup(Boolean.FALSE);

        this.underTest.reindexIfOutdated();

        verifyNoInteractions(this.chatbotIndexingService);
    }

    /**
     * Une API Gemini indisponible ne doit pas empêcher l'application de démarrer.
     */
    @Test
    void testSwallowsIndexingFailure() {
        when(this.chatbotIndexingService.reindexIfOutdated())
                .thenThrow(new IllegalStateException("API Gemini indisponible"));

        assertDoesNotThrow(() -> this.underTest.reindexIfOutdated());
    }

}
