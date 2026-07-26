package org.mosqueethonon.chatbot.exception;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renseignements <em>best-effort</em> extraits du corps d'une réponse 429 de l'API Gemini.
 * <p>
 * Attention à la portée de cette classe : la documentation Gemini garantit le code de statut 429
 * (RESOURCE_EXHAUSTED) mais ne documente <strong>aucune</strong> structure de corps — ni le tableau
 * {@code details}, ni {@code QuotaFailure}, ni {@code RetryInfo}. Ce qui est lu ici provient de
 * payloads réellement observés et peut disparaître sans préavis. Rien ici ne doit donc être
 * indispensable : en l'absence d'information exploitable on renvoie {@link Scope#UNKNOWN} et aucun
 * délai, et l'appelant doit rester correct dans ce cas.
 * <p>
 * L'analyse est faite par expressions régulières et non par un parseur JSON, précisément parce que
 * le corps peut arriver tronqué (voir {@code GeminiRestClientConfig}) et donc syntaxiquement invalide.
 */
public record GeminiQuotaDetails(Scope scope, OptionalInt retryAfterSeconds) {

    public enum Scope {
        /** Limite par minute : réessayer dans quelques secondes a du sens. */
        MINUTE,
        /** Limite journalière : inutile de réessayer avant le lendemain. */
        DAY,
        /** Rien d'exploitable dans le corps — cas nominal si Google change de format. */
        UNKNOWN
    }

    private static final Pattern RETRY_DELAY_PATTERN =
            Pattern.compile("\"retryDelay\"\\s*:\\s*\"(\\d+)(?:\\.\\d+)?s\"");

    private static final Pattern PER_DAY_PATTERN = Pattern.compile("PerDay", Pattern.CASE_INSENSITIVE);

    private static final Pattern PER_MINUTE_PATTERN = Pattern.compile("PerMinute", Pattern.CASE_INSENSITIVE);

    public static GeminiQuotaDetails parse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return new GeminiQuotaDetails(Scope.UNKNOWN, OptionalInt.empty());
        }
        return new GeminiQuotaDetails(parseScope(responseBody), parseRetryAfterSeconds(responseBody));
    }

    /**
     * Le marqueur est cherché dans tout le corps plutôt que dans un champ précis : « PerDay » et
     * « PerMinute » n'apparaissent que dans les identifiants de quota, et cette approche survit à
     * une troncature du corps comme à un déplacement du champ.
     */
    private static Scope parseScope(String responseBody) {
        if (PER_DAY_PATTERN.matcher(responseBody).find()) {
            return Scope.DAY;
        }
        if (PER_MINUTE_PATTERN.matcher(responseBody).find()) {
            return Scope.MINUTE;
        }
        return Scope.UNKNOWN;
    }

    private static OptionalInt parseRetryAfterSeconds(String responseBody) {
        Matcher matcher = RETRY_DELAY_PATTERN.matcher(responseBody);
        if (!matcher.find()) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException e) {
            // Délai aberrant (débordement d'entier) : on préfère ne rien annoncer qu'annoncer faux.
            return OptionalInt.empty();
        }
    }

}
