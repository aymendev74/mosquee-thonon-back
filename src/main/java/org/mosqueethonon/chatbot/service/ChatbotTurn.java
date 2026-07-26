package org.mosqueethonon.chatbot.service;

import org.mosqueethonon.chatbot.enums.ChatbotRoleEnum;

/**
 * Un tour de conversation (question ou réponse) passé à la génération pour le multi-tours.
 */
public record ChatbotTurn(ChatbotRoleEnum role, String content) {
}
