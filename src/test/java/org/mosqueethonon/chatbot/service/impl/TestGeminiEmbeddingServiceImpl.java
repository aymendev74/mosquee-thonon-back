package org.mosqueethonon.chatbot.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.chatbot.ChatbotTestProperties;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.config.GeminiRestClientConfig;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verrouille le contrat HTTP des appels embedContent / batchEmbedContents. L'URI attendue est
 * asserée en chaîne exacte : c'est ce qui fait échouer le test si le préfixe {@code models/}
 * repasse dans une variable d'URI et se retrouve encodé en {@code %2F} (cause du 404 initial).
 */
public class TestGeminiEmbeddingServiceImpl {

    private static final String EMBED_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    private static final String BATCH_EMBED_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:batchEmbedContents";

    private MockRestServiceServer server;

    private GeminiEmbeddingServiceImpl underTest;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        ChatbotProperties properties = ChatbotTestProperties.build();
        RestClient geminiRestClient = new GeminiRestClientConfig()
                .geminiRestClient(builder, properties, new SimpleMeterRegistry());
        this.underTest = new GeminiEmbeddingServiceImpl(geminiRestClient, properties);
    }

    @Test
    void testEmbedCallsExpectedUriWithoutEncodedSlash() {
        this.server.expect(requestTo(EMBED_URL))
                .andExpect(requestTo(not(containsString("%2F"))))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(GeminiRestClientConfig.API_KEY_HEADER, ChatbotTestProperties.API_KEY))
                .andRespond(withSuccess("{\"embedding\":{\"values\":[0.1,0.2,0.3]}}", MediaType.APPLICATION_JSON));

        float[] result = this.underTest.embed("Comment inscrire un élève ?");

        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, result, 0.0001f);
        this.server.verify();
    }

    @Test
    void testEmbedSendsQueryTaskTypeAndConfiguredDimension() {
        this.server.expect(requestTo(EMBED_URL))
                .andExpect(jsonPath("$.model").value("models/gemini-embedding-001"))
                .andExpect(jsonPath("$.taskType").value("RETRIEVAL_QUERY"))
                .andExpect(jsonPath("$.outputDimensionality").value(768))
                .andExpect(jsonPath("$.content.parts[0].text").value("ma question"))
                .andRespond(withSuccess("{\"embedding\":{\"values\":[1.0]}}", MediaType.APPLICATION_JSON));

        this.underTest.embed("ma question");

        this.server.verify();
    }

    @Test
    void testEmbedThrowsWhenResponseHasNoEmbedding() {
        this.server.expect(requestTo(EMBED_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class, () -> this.underTest.embed("ma question"));
        this.server.verify();
    }

    @Test
    void testEmbedBatchSendsDocumentTaskTypeOnEverySubRequest() {
        this.server.expect(requestTo(BATCH_EMBED_URL))
                .andExpect(requestTo(not(containsString("%2F"))))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(GeminiRestClientConfig.API_KEY_HEADER, ChatbotTestProperties.API_KEY))
                .andExpect(jsonPath("$.requests.length()").value(2))
                .andExpect(jsonPath("$.requests[0].model").value("models/gemini-embedding-001"))
                .andExpect(jsonPath("$.requests[0].taskType").value("RETRIEVAL_DOCUMENT"))
                .andExpect(jsonPath("$.requests[0].outputDimensionality").value(768))
                .andExpect(jsonPath("$.requests[1].taskType").value("RETRIEVAL_DOCUMENT"))
                .andExpect(jsonPath("$.requests[1].outputDimensionality").value(768))
                .andRespond(withSuccess(batchResponse(0, 2), MediaType.APPLICATION_JSON));

        List<float[]> result = this.underTest.embedBatch(List.of("chunk A", "chunk B"));

        assertEquals(2, result.size());
        this.server.verify();
    }

    @Test
    void testEmbedBatchSplitsAboveMaxBatchSizeAndPreservesOrder() {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            texts.add("chunk " + i);
        }
        // Deux appels attendus (limite Google de 100 par lot), dans l'ordre.
        this.server.expect(requestTo(BATCH_EMBED_URL))
                .andExpect(jsonPath("$.requests.length()").value(100))
                .andRespond(withSuccess(batchResponse(0, 100), MediaType.APPLICATION_JSON));
        this.server.expect(requestTo(BATCH_EMBED_URL))
                .andExpect(jsonPath("$.requests.length()").value(50))
                .andExpect(jsonPath("$.requests[0].content.parts[0].text").value("chunk 100"))
                .andRespond(withSuccess(batchResponse(100, 50), MediaType.APPLICATION_JSON));

        List<float[]> result = this.underTest.embedBatch(texts);

        assertEquals(150, result.size());
        for (int i = 0; i < 150; i++) {
            assertEquals((float) i, result.get(i)[0], 0.0001f, "embedding hors séquence à l'index " + i);
        }
        this.server.verify();
    }

    /**
     * Une réponse incomplète doit échouer bruyamment : l'indexation ré-associe les embeddings aux
     * chunks par position, un décalage corromprait silencieusement tout l'index.
     */
    @Test
    void testEmbedBatchThrowsWhenResponseHasFewerEmbeddingsThanRequested() {
        this.server.expect(requestTo(BATCH_EMBED_URL))
                .andRespond(withSuccess(batchResponse(0, 2), MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> this.underTest.embedBatch(List.of("chunk A", "chunk B", "chunk C")));

        assertTrue(exception.getMessage().contains("2 au lieu de 3"), exception.getMessage());
        this.server.verify();
    }

    @Test
    void testEmbedBatchThrowsWhenResponseHasNoEmbeddings() {
        this.server.expect(requestTo(BATCH_EMBED_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class, () -> this.underTest.embedBatch(List.of("chunk A")));
        this.server.verify();
    }

    /**
     * Réponse batch dont chaque embedding porte son index en unique valeur, ce qui permet de
     * vérifier que l'ordre des textes d'entrée est préservé à travers les lots.
     */
    private static String batchResponse(int firstIndex, int count) {
        StringBuilder json = new StringBuilder("{\"embeddings\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"values\":[").append(firstIndex + i).append(".0]}");
        }
        return json.append("]}").toString();
    }

}
