package org.mosqueethonon.chatbot;

import org.mosqueethonon.chatbot.config.ChatbotProperties;

/**
 * Fabrique une configuration chatbot cohérente pour les tests de la couche HTTP Gemini, avec les
 * mêmes valeurs que {@code application.yml} (hors clé d'API).
 */
public final class ChatbotTestProperties {

    public static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    public static final String API_KEY = "test-key";

    public static final String EMBEDDING_MODEL = "gemini-embedding-001";

    public static final String GENERATION_MODEL = "gemini-2.5-flash";

    public static final int EMBEDDING_DIMENSION = 768;

    public static final double TEMPERATURE = 0.2d;

    public static final int CHECK_INTERVAL_SECONDS = 300;

    private ChatbotTestProperties() {
    }

    public static ChatbotProperties build() {
        ChatbotProperties.Gemini gemini = new ChatbotProperties.Gemini();
        gemini.setBaseUrl(BASE_URL);
        gemini.setApiKey(API_KEY);
        gemini.setEmbeddingModel(EMBEDDING_MODEL);
        gemini.setEmbeddingDimension(EMBEDDING_DIMENSION);
        gemini.setGenerationModel(GENERATION_MODEL);
        gemini.setTemperature(TEMPERATURE);

        ChatbotProperties.Retrieval retrieval = new ChatbotProperties.Retrieval();
        retrieval.setTopK(5);
        retrieval.setMinScore(0.55d);

        ChatbotProperties.Indexing indexing = new ChatbotProperties.Indexing();
        indexing.setEnabled(Boolean.TRUE);
        indexing.setCheckInterval(CHECK_INTERVAL_SECONDS);

        ChatbotProperties properties = new ChatbotProperties();
        properties.setGemini(gemini);
        properties.setRetrieval(retrieval);
        properties.setIndexing(indexing);
        properties.setDocsPath("docs/functional");
        return properties;
    }

}
