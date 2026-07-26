package org.mosqueethonon.chatbot.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le contenu du corps d'un 429 Gemini n'est pas documenté par Google : ces tests figent surtout le
 * comportement de <em>dégradation</em>. Si le format change, on doit obtenir UNKNOWN sans délai,
 * jamais une exception ni une valeur inventée.
 */
public class TestGeminiQuotaDetails {

    private static final String FULL_DAILY_QUOTA_BODY = """
            {"error":{"code":429,"message":"Resource exhausted.","status":"RESOURCE_EXHAUSTED","details":[
              {"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[
                {"quotaMetric":"generativelanguage.googleapis.com/generate_content_free_tier_requests",
                 "quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier","quotaValue":"250"}]},
              {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"14s"}]}}""";

    private static final String PER_MINUTE_QUOTA_BODY = """
            {"error":{"code":429,"status":"RESOURCE_EXHAUSTED","details":[
              {"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[
                {"quotaId":"GenerateRequestsPerMinutePerProjectPerModel-FreeTier"}]},
              {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"27s"}]}}""";

    @Test
    void testParsesDailyQuotaAndRetryDelay() {
        GeminiQuotaDetails details = GeminiQuotaDetails.parse(FULL_DAILY_QUOTA_BODY);

        assertEquals(GeminiQuotaDetails.Scope.DAY, details.scope());
        assertTrue(details.retryAfterSeconds().isPresent());
        assertEquals(14, details.retryAfterSeconds().getAsInt());
    }

    @Test
    void testParsesPerMinuteQuota() {
        GeminiQuotaDetails details = GeminiQuotaDetails.parse(PER_MINUTE_QUOTA_BODY);

        assertEquals(GeminiQuotaDetails.Scope.MINUTE, details.scope());
        assertEquals(27, details.retryAfterSeconds().getAsInt());
    }

    /**
     * Cas nominal si Google cesse de renvoyer le tableau details : le corps reste un 429 valide mais
     * ne dit plus rien d'exploitable.
     */
    @Test
    void testDegradesToUnknownWhenBodyHasNoDetails() {
        GeminiQuotaDetails details = GeminiQuotaDetails.parse(
                "{\"error\":{\"code\":429,\"message\":\"Resource exhausted.\",\"status\":\"RESOURCE_EXHAUSTED\"}}");

        assertEquals(GeminiQuotaDetails.Scope.UNKNOWN, details.scope());
        assertFalse(details.retryAfterSeconds().isPresent());
    }

    /**
     * Le corps est tronqué à 2000 caractères avant d'atteindre l'exception : il peut donc être du
     * JSON syntaxiquement invalide, ce qui ferait échouer un parseur mais pas une regex.
     */
    @Test
    void testHandlesTruncatedBodyWithoutFailing() {
        // Coupe juste avant le bloc RetryInfo : le quotaId est encore là, le délai a disparu, et le
        // JSON obtenu est syntaxiquement invalide.
        String truncated = FULL_DAILY_QUOTA_BODY.substring(0, FULL_DAILY_QUOTA_BODY.indexOf("retryDelay"))
                + "…(tronqué)";

        GeminiQuotaDetails details = GeminiQuotaDetails.parse(truncated);

        assertEquals(GeminiQuotaDetails.Scope.DAY, details.scope());
        assertFalse(details.retryAfterSeconds().isPresent(), "le retryDelay tronqué ne doit pas être inventé");
    }

    @Test
    void testHandlesNullAndBlankBody() {
        assertEquals(GeminiQuotaDetails.Scope.UNKNOWN, GeminiQuotaDetails.parse(null).scope());
        assertEquals(GeminiQuotaDetails.Scope.UNKNOWN, GeminiQuotaDetails.parse("   ").scope());
        assertFalse(GeminiQuotaDetails.parse(null).retryAfterSeconds().isPresent());
    }

    @Test
    void testIgnoresAberrantRetryDelay() {
        GeminiQuotaDetails details = GeminiQuotaDetails.parse(
                "{\"details\":[{\"retryDelay\":\"99999999999999999999s\"}]}");

        assertFalse(details.retryAfterSeconds().isPresent());
    }

    @Test
    void testIsQuotaExceededRestsOnStatusOnly() {
        GeminiApiException quota = new GeminiApiException(
                org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, FULL_DAILY_QUOTA_BODY);
        GeminiApiException other = new GeminiApiException(
                org.springframework.http.HttpStatus.BAD_REQUEST, FULL_DAILY_QUOTA_BODY);

        assertTrue(quota.isQuotaExceeded());
        // Même corps, statut différent : le corps ne doit jamais servir à décider.
        assertFalse(other.isQuotaExceeded());
    }

}
