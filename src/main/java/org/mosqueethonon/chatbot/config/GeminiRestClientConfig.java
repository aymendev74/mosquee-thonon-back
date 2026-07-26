package org.mosqueethonon.chatbot.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.exception.GeminiApiException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * Construit le {@link RestClient} partagé par tous les appels à l'API Google Generative Language.
 * Centraliser la configuration ici garantit que l'authentification et la gestion d'erreur sont
 * identiques sur les trois endpoints appelés (embedContent, batchEmbedContents, generateContent).
 */
@Configuration
@Slf4j
public class GeminiRestClientConfig {

    public static final String API_KEY_HEADER = "x-goog-api-key";

    private static final int MAX_LOGGED_ERROR_BODY_LENGTH = 2000;

    /**
     * La clé d'API passe par un header et non par le paramètre de requête {@code ?key=} : c'est la
     * forme documentée par Google, et cela évite que la clé se retrouve dans les URI loguées ou dans
     * les messages d'exception.
     */
    @Bean
    public RestClient geminiRestClient(RestClient.Builder restClientBuilder, ChatbotProperties chatbotProperties,
                                       MeterRegistry meterRegistry) {
        ChatbotProperties.Gemini gemini = chatbotProperties.getGemini();
        return restClientBuilder
                .baseUrl(gemini.getBaseUrl())
                .defaultHeader(API_KEY_HEADER, gemini.getApiKey())
                .requestInterceptor(new GeminiMetricsInterceptor(meterRegistry))
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String body = truncate(StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
                    log.error("Appel Gemini en erreur : {} {} -> HTTP {} : {}", request.getMethod(),
                            request.getURI(), response.getStatusCode().value(), body);
                    throw new GeminiApiException(response.getStatusCode(), body);
                })
                .build();
    }

    private static String truncate(String body) {
        return body.length() > MAX_LOGGED_ERROR_BODY_LENGTH
                ? body.substring(0, MAX_LOGGED_ERROR_BODY_LENGTH) + "…(tronqué)"
                : body;
    }

}
