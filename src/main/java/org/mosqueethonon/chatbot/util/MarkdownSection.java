package org.mosqueethonon.chatbot.util;

/**
 * Une section markdown (titre H2/H3 + contenu jusqu'à la section suivante), unité de chunk
 * pour l'indexation du chatbot.
 */
public record MarkdownSection(String title, String content) {
}
