package org.mosqueethonon.chatbot.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Levée lorsqu'un appel à l'API Google Generative Language renvoie un statut d'erreur. Contrairement
 * à {@code HttpClientErrorException}, elle conserve le corps de la réponse, qui contient le détail
 * de l'erreur Google ({@code {"error":{"code":...,"message":...,"status":...}}}).
 */
@Getter
public class GeminiApiException extends RuntimeException {

    /**
     * Le corps est tronqué dans le message car celui-ci est re-logué intégralement par le
     * gestionnaire d'exception global, avec la stacktrace.
     */
    private static final int MAX_MESSAGE_BODY_LENGTH = 500;

    private final HttpStatusCode status;

    private final String responseBody;

    public GeminiApiException(HttpStatusCode status, String responseBody) {
        super("Erreur API Gemini (HTTP " + status.value() + ") : " + truncate(responseBody));
        this.status = status;
        this.responseBody = responseBody;
    }

    /**
     * Seul signal fiable de dépassement de quota : la documentation Gemini garantit le statut 429
     * (RESOURCE_EXHAUSTED) pour tout dépassement de limite (RPM, TPM, RPD, dépense). Le contenu du
     * corps, lui, n'est pas contractuel — voir {@link GeminiQuotaDetails}.
     */
    public boolean isQuotaExceeded() {
        return this.status.value() == 429;
    }

    public GeminiQuotaDetails getQuotaDetails() {
        return GeminiQuotaDetails.parse(this.responseBody);
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > MAX_MESSAGE_BODY_LENGTH
                ? body.substring(0, MAX_MESSAGE_BODY_LENGTH) + "…(tronqué)"
                : body;
    }

}
