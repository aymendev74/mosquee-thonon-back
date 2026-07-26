package org.mosqueethonon.chatbot.repository;

import org.mosqueethonon.chatbot.entity.ChatbotDocumentChunkEntity;

/**
 * Résultat d'une recherche de similarité : un chunk et son score de similarité cosinus
 * (1 - distance cosinus pgvector), borné entre -1 et 1 (proche de 1 = très similaire).
 */
public record ChatbotChunkMatch(ChatbotDocumentChunkEntity chunk, double score) {
}
