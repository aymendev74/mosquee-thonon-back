package org.mosqueethonon.chatbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;
import org.mosqueethonon.chatbot.repository.ChatbotDocumentChunkRepository;
import org.mosqueethonon.chatbot.service.ChatbotRetrievalService;
import org.mosqueethonon.chatbot.service.EmbeddingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotRetrievalServiceImpl implements ChatbotRetrievalService {

    private final EmbeddingService embeddingService;

    private final ChatbotDocumentChunkRepository chatbotDocumentChunkRepository;

    private final ChatbotProperties chatbotProperties;

    @Override
    public List<ChatbotChunkMatch> retrieve(String question) {
        float[] questionEmbedding = this.embeddingService.embed(question);

        int topK = this.chatbotProperties.getRetrieval().getTopK();
        double minScore = this.chatbotProperties.getRetrieval().getMinScore();

        List<ChatbotChunkMatch> matches = this.chatbotDocumentChunkRepository.findTopKBySimilarity(questionEmbedding, topK);

        List<ChatbotChunkMatch> retained = matches.stream()
                .filter(match -> match.score() >= minScore)
                .toList();

        log.debug("Retrieval: {} candidats, {} retenus au-dessus du seuil {}", matches.size(), retained.size(), minScore);

        return retained;
    }

}
