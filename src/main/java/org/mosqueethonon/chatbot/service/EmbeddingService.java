package org.mosqueethonon.chatbot.service;

import java.util.List;

public interface EmbeddingService {

    /**
     * Calcule l'embedding d'un texte.
     */
    float[] embed(String text);

    /**
     * Calcule les embeddings d'une liste de textes en un minimum d'appels réseau (batch API).
     * L'ordre des embeddings renvoyés correspond à l'ordre des textes fournis.
     */
    List<float[]> embedBatch(List<String> texts);

}
