package org.mosqueethonon.chatbot.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.chatbot.ChatbotTestProperties;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.config.GeminiRestClientConfig;
import org.mosqueethonon.chatbot.enums.ChatbotRoleEnum;
import org.mosqueethonon.chatbot.service.ChatbotTurn;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verrouille le contrat HTTP de l'appel generateContent : URI exacte (sans {@code %2F} ni paramètre
 * de requête {@code key}, la clé passant désormais par un header) et forme du payload RAG.
 */
public class TestGeminiGenerationServiceImpl {

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final String OK_RESPONSE =
            "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"La réponse.\"}]}}]}";

    private MockRestServiceServer server;

    private GeminiGenerationServiceImpl underTest;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        ChatbotProperties properties = ChatbotTestProperties.build();
        RestClient geminiRestClient = new GeminiRestClientConfig()
                .geminiRestClient(builder, properties, new SimpleMeterRegistry());
        this.underTest = new GeminiGenerationServiceImpl(geminiRestClient, properties);
    }

    @Test
    void testGenerateCallsExpectedUriWithApiKeyInHeader() {
        this.server.expect(requestTo(GENERATE_URL))
                .andExpect(requestTo(not(containsString("%2F"))))
                .andExpect(requestTo(not(containsString("key="))))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(GeminiRestClientConfig.API_KEY_HEADER, ChatbotTestProperties.API_KEY))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        String result = this.underTest.generate(List.of(), "un extrait", "ma question");

        assertEquals("La réponse.", result);
        this.server.verify();
    }

    @Test
    void testGenerateSendsSystemInstructionAndMapsHistoryRoles() {
        this.server.expect(requestTo(GENERATE_URL))
                .andExpect(jsonPath("$.systemInstruction.parts[0].text")
                        .value(GeminiGenerationServiceImpl.SYSTEM_INSTRUCTION))
                .andExpect(jsonPath("$.contents.length()").value(3))
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(jsonPath("$.contents[1].role").value("model"))
                .andExpect(jsonPath("$.contents[2].role").value("user"))
                .andExpect(jsonPath("$.contents[2].parts[0].text").value(containsString("CONTEXTE:\nun extrait")))
                .andExpect(jsonPath("$.contents[2].parts[0].text").value(containsString("QUESTION: ma question")))
                .andExpect(jsonPath("$.generationConfig.temperature").value(ChatbotTestProperties.TEMPERATURE))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        List<ChatbotTurn> history = List.of(
                new ChatbotTurn(ChatbotRoleEnum.USER, "question précédente"),
                new ChatbotTurn(ChatbotRoleEnum.ASSISTANT, "réponse précédente"));

        this.underTest.generate(history, "un extrait", "ma question");

        this.server.verify();
    }

    @Test
    void testGenerateUsesPlaceholderWhenContextIsBlank() {
        this.server.expect(requestTo(GENERATE_URL))
                .andExpect(jsonPath("$.contents[0].parts[0].text")
                        .value(containsString("(aucun extrait de documentation pertinent trouvé)")))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        this.underTest.generate(List.of(), "   ", "ma question");

        this.server.verify();
    }

    @Test
    void testGenerateThrowsWhenNoCandidate() {
        this.server.expect(requestTo(GENERATE_URL))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> this.underTest.generate(List.of(), "un extrait", "ma question"));
        this.server.verify();
    }

    /**
     * Forme réelle d'une réponse bloquée par les filtres de sécurité : le candidat existe mais n'a
     * ni content ni parts.
     */
    @Test
    void testGenerateThrowsWhenCandidateHasNoContent() {
        this.server.expect(requestTo(GENERATE_URL))
                .andRespond(withSuccess("{\"candidates\":[{\"finishReason\":\"SAFETY\"}]}",
                        MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> this.underTest.generate(List.of(), "un extrait", "ma question"));
        this.server.verify();
    }

    @Test
    void testGenerateThrowsWhenCandidateHasEmptyParts() {
        this.server.expect(requestTo(GENERATE_URL))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[]}}]}",
                        MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> this.underTest.generate(List.of(), "un extrait", "ma question"));
        this.server.verify();
    }

    /**
     * La réponse est affichée dans une bulle de chat étroite qui ne rend qu'un sous-ensemble du
     * markdown (gras et listes à puces) : l'instruction système doit continuer à interdire les
     * constructions non rendues, faute de quoi elles s'afficheraient telles quelles.
     */
    @Test
    void testSystemInstructionRestrictsOutputFormat() {
        String instruction = GeminiGenerationServiceImpl.SYSTEM_INSTRUCTION;

        assertTrue(instruction.contains("FORMAT DE RÉPONSE"), "le cadrage du format doit être présent");
        assertTrue(instruction.contains("**terme**"), "le gras doit rester autorisé");
        assertTrue(instruction.contains("\"- \""), "les listes à puces doivent rester autorisées");
        assertTrue(instruction.contains("N'utilise JAMAIS de titres (#), de tableaux (|), de blocs de code (```)"),
                "titres, tableaux et blocs de code doivent être explicitement interdits");
    }

}
