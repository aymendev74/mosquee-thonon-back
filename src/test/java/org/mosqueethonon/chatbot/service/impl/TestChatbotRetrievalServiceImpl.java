package org.mosqueethonon.chatbot.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.entity.ChatbotDocumentChunkEntity;
import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;
import org.mosqueethonon.chatbot.repository.ChatbotDocumentChunkRepository;
import org.mosqueethonon.chatbot.service.EmbeddingService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestChatbotRetrievalServiceImpl {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private ChatbotDocumentChunkRepository chatbotDocumentChunkRepository;

    @Mock
    private ChatbotProperties chatbotProperties;

    @InjectMocks
    private ChatbotRetrievalServiceImpl underTest;

    private ChatbotProperties.Retrieval retrieval;

    @BeforeEach
    public void setUp() {
        this.retrieval = new ChatbotProperties.Retrieval();
        this.retrieval.setTopK(5);
        this.retrieval.setMinScore(0.5);
    }

    private ChatbotChunkMatch match(String theme, double score) {
        ChatbotDocumentChunkEntity chunk = ChatbotDocumentChunkEntity.builder()
                .id(1L)
                .theme(theme)
                .sectionTitle("Section")
                .content("Contenu")
                .build();
        return new ChatbotChunkMatch(chunk, score);
    }

    @Test
    public void testRetrieveFiltersOutMatchesBelowThreshold() {
        // GIVEN
        when(this.chatbotProperties.getRetrieval()).thenReturn(this.retrieval);
        when(this.embeddingService.embed("Quels sont les tarifs ?")).thenReturn(new float[]{0.1f, 0.2f});
        when(this.chatbotDocumentChunkRepository.findTopKBySimilarity(any(), anyInt()))
                .thenReturn(List.of(match("tarifs", 0.8), match("adhesions", 0.3)));

        // WHEN
        List<ChatbotChunkMatch> result = underTest.retrieve("Quels sont les tarifs ?");

        // THEN
        assertEquals(1, result.size());
        assertEquals("tarifs", result.get(0).chunk().getTheme());
    }

    @Test
    public void testRetrieveReturnsEmptyListWhenNoMatchAboveThreshold() {
        // GIVEN
        when(this.chatbotProperties.getRetrieval()).thenReturn(this.retrieval);
        when(this.embeddingService.embed(Mockito.anyString())).thenReturn(new float[]{0.1f});
        when(this.chatbotDocumentChunkRepository.findTopKBySimilarity(any(), anyInt()))
                .thenReturn(List.of(match("hors-sujet", 0.1)));

        // WHEN
        List<ChatbotChunkMatch> result = underTest.retrieve("Question hors périmètre");

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRetrievePassesTopKFromConfiguration() {
        // GIVEN
        this.retrieval.setTopK(3);
        when(this.chatbotProperties.getRetrieval()).thenReturn(this.retrieval);
        when(this.embeddingService.embed(Mockito.anyString())).thenReturn(new float[]{0.1f});
        when(this.chatbotDocumentChunkRepository.findTopKBySimilarity(any(), anyInt())).thenReturn(List.of());

        // WHEN
        underTest.retrieve("Question");

        // THEN
        Mockito.verify(this.chatbotDocumentChunkRepository).findTopKBySimilarity(any(), org.mockito.ArgumentMatchers.eq(3));
    }

}
