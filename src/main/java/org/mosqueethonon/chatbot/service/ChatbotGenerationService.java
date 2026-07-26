package org.mosqueethonon.chatbot.service;

import java.util.List;

public interface ChatbotGenerationService {

    /**
     * Génère la réponse à la {@code question}, à partir du {@code context} récupéré (chunks
     * documentaires, peut être vide) et de l'{@code history} des tours précédents de la
     * conversation (pour le multi-tours).
     */
    String generate(List<ChatbotTurn> history, String context, String question);

}
