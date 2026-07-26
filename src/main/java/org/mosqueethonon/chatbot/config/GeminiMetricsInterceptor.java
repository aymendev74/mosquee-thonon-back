package org.mosqueethonon.chatbot.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.net.URI;

/**
 * Compte <strong>tous</strong> les appels à l'API Gemini, pas seulement ceux en échec.
 * <p>
 * C'est délibéré : un compteur de 429 est un indicateur <em>retardé</em>, il ne s'incrémente qu'une
 * fois le quota déjà atteint et l'utilisateur déjà impacté. Seul le comptage des appels
 * <em>réussis</em>, rapporté au quota journalier connu, permet d'anticiper — d'où l'étiquette
 * {@code outcome} qui distingue succès, quota dépassé et autres erreurs.
 * <p>
 * Placé en intercepteur plutôt que dans le gestionnaire de statut : ce dernier n'est appelé que sur
 * les réponses en erreur et ne verrait donc jamais un succès.
 */
@RequiredArgsConstructor
public class GeminiMetricsInterceptor implements ClientHttpRequestInterceptor {

    static final String METRIC_NAME = "chatbot.gemini.requests";

    static final String OUTCOME_SUCCESS = "success";

    static final String OUTCOME_QUOTA_EXCEEDED = "quota_exceeded";

    static final String OUTCOME_ERROR = "error";

    static final String OUTCOME_IO_ERROR = "io_error";

    private static final String UNKNOWN = "unknown";

    private final MeterRegistry meterRegistry;

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
                                        @NonNull ClientHttpRequestExecution execution) throws IOException {
        String operation = extractOperation(request.getURI());
        String model = extractModel(request.getURI());
        try {
            ClientHttpResponse response = execution.execute(request, body);
            count(operation, model, outcomeOf(response.getStatusCode().value()));
            return response;
        } catch (IOException | RuntimeException e) {
            // Panne réseau ou timeout : aucune réponse HTTP, mais l'appel a bien été tenté.
            count(operation, model, OUTCOME_IO_ERROR);
            throw e;
        }
    }

    private void count(String operation, String model, String outcome) {
        Counter.builder(METRIC_NAME)
                .description("Appels à l'API Google Generative Language, par opération et par issue")
                .tag("operation", operation)
                .tag("model", model)
                .tag("outcome", outcome)
                .register(this.meterRegistry)
                .increment();
    }

    private static String outcomeOf(int statusCode) {
        if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return OUTCOME_QUOTA_EXCEEDED;
        }
        return HttpStatus.valueOf(statusCode).is2xxSuccessful() ? OUTCOME_SUCCESS : OUTCOME_ERROR;
    }

    /**
     * {@code /v1beta/models/gemini-2.5-flash:generateContent} donne {@code generateContent}.
     * Cardinalité volontairement faible : trois opérations possibles au maximum.
     */
    private static String extractOperation(URI uri) {
        String path = uri.getPath();
        int separator = path.lastIndexOf(':');
        return separator >= 0 && separator < path.length() - 1 ? path.substring(separator + 1) : UNKNOWN;
    }

    /**
     * Le quota Gemini étant compté par modèle, l'étiquette permet de savoir lequel sature.
     */
    private static String extractModel(URI uri) {
        String path = uri.getPath();
        int start = path.lastIndexOf("/models/");
        if (start < 0) {
            return UNKNOWN;
        }
        String remainder = path.substring(start + "/models/".length());
        int separator = remainder.indexOf(':');
        String model = separator >= 0 ? remainder.substring(0, separator) : remainder;
        return model.isBlank() ? UNKNOWN : model;
    }

}
