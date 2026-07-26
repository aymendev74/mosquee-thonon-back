package org.mosqueethonon.chatbot.service;

import org.mosqueethonon.chatbot.enums.ChatbotFeedbackEnum;
import org.mosqueethonon.v1.dto.chatbot.ChatbotMessageRequestDto;
import org.mosqueethonon.v1.dto.chatbot.ChatbotMessageResponseDto;

public interface ChatbotService {

    /**
     * Un tour de chat : charge/crée la conversation, retrieval, génération, persiste les
     * messages USER puis ASSISTANT (avec sources), renvoie la réponse.
     */
    ChatbotMessageResponseDto sendMessage(ChatbotMessageRequestDto request);

    /**
     * Enregistre le feedback (pouce haut/bas) de l'utilisateur sur un message.
     */
    void setFeedback(Long messageId, ChatbotFeedbackEnum feedback);

}
