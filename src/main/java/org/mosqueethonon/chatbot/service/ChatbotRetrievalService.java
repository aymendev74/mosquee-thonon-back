package org.mosqueethonon.chatbot.service;

import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;

import java.util.List;

public interface ChatbotRetrievalService {

    /**
     * Embed la question, recherche les {@code chatbot.retrieval.top-k} chunks les plus
     * proches, puis ne conserve que ceux dont le score de similarité est au moins égal à
     * {@code chatbot.retrieval.min-score}. Peut renvoyer une liste vide (aucun chunk pertinent).
     */
    List<ChatbotChunkMatch> retrieve(String question);

}
