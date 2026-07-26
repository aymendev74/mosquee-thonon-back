package org.mosqueethonon.chatbot.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.chatbot.ChatbotTestProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Le point central de ces tests est que les appels <strong>réussis</strong> sont comptés : c'est la
 * seule mesure qui permet d'anticiper la saturation du quota, un compteur de 429 n'arrivant par
 * construction qu'après coup.
 */
public class TestGeminiMetricsInterceptor {

    private static final String EMBED_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private MeterRegistry meterRegistry;

    private MockRestServiceServer server;

    private RestClient geminiRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.meterRegistry = new SimpleMeterRegistry();
        this.geminiRestClient = new GeminiRestClientConfig()
                .geminiRestClient(builder, ChatbotTestProperties.build(), this.meterRegistry);
    }

    @Test
    void testCountsSuccessfulCallsWithOperationAndModel() {
        this.server.expect(requestTo(EMBED_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        this.geminiRestClient.get().uri(EMBED_URL).retrieve().body(String.class);

        assertEquals(1.0, count("embedContent", "gemini-embedding-001",
                GeminiMetricsInterceptor.OUTCOME_SUCCESS));
    }

    @Test
    void testCountsQuotaExceededSeparatelyFromOtherErrors() {
        this.server.expect(requestTo(GENERATE_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON).body("{\"error\":{\"code\":429}}"));

        assertThrows(RuntimeException.class,
                () -> this.geminiRestClient.get().uri(GENERATE_URL).retrieve().body(String.class));

        assertEquals(1.0, count("generateContent", "gemini-2.5-flash",
                GeminiMetricsInterceptor.OUTCOME_QUOTA_EXCEEDED));
        assertEquals(0.0, count("generateContent", "gemini-2.5-flash",
                GeminiMetricsInterceptor.OUTCOME_ERROR));
    }

    @Test
    void testCountsOtherFailuresAsError() {
        this.server.expect(requestTo(GENERATE_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON).body("{\"error\":{\"code\":500}}"));

        assertThrows(RuntimeException.class,
                () -> this.geminiRestClient.get().uri(GENERATE_URL).retrieve().body(String.class));

        assertEquals(1.0, count("generateContent", "gemini-2.5-flash",
                GeminiMetricsInterceptor.OUTCOME_ERROR));
    }

    /**
     * Plusieurs appels réussis doivent s'accumuler sur la même série : c'est cette somme qu'on
     * compare au quota journalier pour déclencher une alerte avant saturation.
     */
    @Test
    void testAccumulatesSuccessfulCalls() {
        for (int i = 0; i < 3; i++) {
            this.server.expect(requestTo(EMBED_URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        }

        for (int i = 0; i < 3; i++) {
            this.geminiRestClient.get().uri(EMBED_URL).retrieve().body(String.class);
        }

        assertEquals(3.0, count("embedContent", "gemini-embedding-001",
                GeminiMetricsInterceptor.OUTCOME_SUCCESS));
    }

    private double count(String operation, String model, String outcome) {
        return this.meterRegistry.find(GeminiMetricsInterceptor.METRIC_NAME)
                .tag("operation", operation)
                .tag("model", model)
                .tag("outcome", outcome)
                .counters().stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();
    }

}
