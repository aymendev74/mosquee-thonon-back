package org.mosqueethonon.chatbot.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.service.EmbeddingService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Appelle l'API Google Generative Language pour calculer des embeddings (modèle
 * gemini-embedding-001 par défaut), via {@link RestClient} (pas de dépendance Spring AI/LangChain4j).
 * <p>
 * La dimension de sortie est explicitement imposée via {@code outputDimensionality} pour correspondre
 * à la colonne pgvector : par défaut le modèle renvoie 3072 dimensions. Le {@code taskType} est
 * asymétrique (RETRIEVAL_QUERY pour la question, RETRIEVAL_DOCUMENT pour les chunks indexés), ce qui
 * est le mode nominal du RAG chez Google.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiEmbeddingServiceImpl implements EmbeddingService {

    // Limite documentée de l'API Google batchEmbedContents
    private static final int MAX_BATCH_SIZE = 100;

    private static final String MODEL_PREFIX = "models/";

    private static final String TASK_TYPE_QUERY = "RETRIEVAL_QUERY";

    private static final String TASK_TYPE_DOCUMENT = "RETRIEVAL_DOCUMENT";

    private final RestClient geminiRestClient;

    private final ChatbotProperties chatbotProperties;

    @Override
    public float[] embed(String text) {
        String rawModel = this.chatbotProperties.getGemini().getEmbeddingModel();
        GeminiEmbedRequest request = buildRequest(text, TASK_TYPE_QUERY);

        GeminiEmbedResponse response = this.geminiRestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/models/{model}:embedContent").build(rawModel))
                .body(request)
                .retrieve()
                .body(GeminiEmbedResponse.class);

        if (response == null || response.embedding() == null) {
            log.error("Réponse vide de l'API Gemini embedContent");
            throw new IllegalStateException("Réponse vide de l'API Gemini embedContent");
        }
        return toFloatArray(response.embedding().values());
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        String rawModel = this.chatbotProperties.getGemini().getEmbeddingModel();
        List<float[]> result = new ArrayList<>(texts.size());

        for (int start = 0; start < texts.size(); start += MAX_BATCH_SIZE) {
            List<String> batch = texts.subList(start, Math.min(start + MAX_BATCH_SIZE, texts.size()));
            List<GeminiEmbedRequest> requests = batch.stream()
                    .map(text -> buildRequest(text, TASK_TYPE_DOCUMENT))
                    .toList();

            GeminiBatchEmbedResponse response = this.geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/models/{model}:batchEmbedContents").build(rawModel))
                    .body(new GeminiBatchEmbedRequest(requests))
                    .retrieve()
                    .body(GeminiBatchEmbedResponse.class);

            if (response == null || response.embeddings() == null) {
                log.error("Réponse vide de l'API Gemini batchEmbedContents");
                throw new IllegalStateException("Réponse vide de l'API Gemini batchEmbedContents");
            }
            // L'appelant ré-associe les embeddings aux chunks par position : une réponse incomplète
            // décalerait silencieusement tous les chunks suivants, il faut donc échouer ici.
            if (response.embeddings().size() != requests.size()) {
                log.error("L'API Gemini batchEmbedContents a renvoyé {} embedding(s) pour {} texte(s)",
                        response.embeddings().size(), requests.size());
                throw new IllegalStateException("Nombre d'embeddings incohérent renvoyé par l'API Gemini : "
                        + response.embeddings().size() + " au lieu de " + requests.size());
            }
            response.embeddings().forEach(embedding -> result.add(toFloatArray(embedding.values())));
        }

        return result;
    }

    /**
     * Le champ {@code model} du corps attend le nom de ressource complet ({@code models/xxx}), alors
     * que l'URI n'en prend que l'identifiant nu.
     */
    private GeminiEmbedRequest buildRequest(String text, String taskType) {
        ChatbotProperties.Gemini gemini = this.chatbotProperties.getGemini();
        return new GeminiEmbedRequest(MODEL_PREFIX + gemini.getEmbeddingModel(),
                new GeminiContent(List.of(new GeminiPart(text))), taskType, gemini.getEmbeddingDimension());
    }

    private float[] toFloatArray(List<Float> values) {
        float[] array = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    private record GeminiPart(String text) {
    }

    private record GeminiContent(List<GeminiPart> parts) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record GeminiEmbedRequest(String model, GeminiContent content, String taskType,
                                      Integer outputDimensionality) {
    }

    private record GeminiEmbeddingValues(List<Float> values) {
    }

    private record GeminiEmbedResponse(GeminiEmbeddingValues embedding) {
    }

    private record GeminiBatchEmbedRequest(List<GeminiEmbedRequest> requests) {
    }

    private record GeminiBatchEmbedResponse(List<GeminiEmbeddingValues> embeddings) {
    }

}
