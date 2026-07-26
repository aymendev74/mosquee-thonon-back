package org.mosqueethonon.chatbot.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.chatbot.ChatbotTestProperties;
import org.mosqueethonon.chatbot.exception.GeminiApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Vérifie que le client Gemini authentifie par header et, surtout, qu'une erreur HTTP remonte avec
 * le corps de réponse de Google — sans quoi un 400/404 reste indiagnosticable.
 */
public class TestGeminiRestClientConfig {

    private static final String ANY_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    private static final String GOOGLE_ERROR_BODY = "{\"error\":{\"code\":400,"
            + "\"message\":\"Invalid value at 'output_dimensionality'\",\"status\":\"INVALID_ARGUMENT\"}}";

    private MockRestServiceServer server;

    private RestClient geminiRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.geminiRestClient = new GeminiRestClientConfig()
                .geminiRestClient(builder, ChatbotTestProperties.build(), new SimpleMeterRegistry());
    }

    @Test
    void testApiKeyIsSentAsDefaultHeader() {
        this.server.expect(requestTo(ANY_URL))
                .andExpect(header(GeminiRestClientConfig.API_KEY_HEADER, ChatbotTestProperties.API_KEY))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        this.geminiRestClient.get().uri(ANY_URL).retrieve().body(String.class);

        this.server.verify();
    }

    @Test
    void testBadRequestIsWrappedWithGoogleErrorBody() {
        this.server.expect(requestTo(ANY_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(GOOGLE_ERROR_BODY));

        GeminiApiException exception = assertThrows(GeminiApiException.class,
                () -> this.geminiRestClient.get().uri(ANY_URL).retrieve().body(String.class));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getResponseBody().contains("INVALID_ARGUMENT"),
                "le corps d'erreur Google doit être conservé");
        assertTrue(exception.getMessage().contains("output_dimensionality"),
                "le message doit exposer le détail Google");
        this.server.verify();
    }

    @Test
    void testNotFoundIsWrappedWithGoogleErrorBody() {
        this.server.expect(requestTo(ANY_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":404,\"message\":\"models/xxx is not found\"}}"));

        GeminiApiException exception = assertThrows(GeminiApiException.class,
                () -> this.geminiRestClient.get().uri(ANY_URL).retrieve().body(String.class));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getResponseBody().contains("is not found"));
        this.server.verify();
    }

}
