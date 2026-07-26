package org.mosqueethonon.configuration.exception;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.chatbot.exception.GeminiApiException;
import org.mosqueethonon.chatbot.v1.dto.ChatbotQuotaErrorDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestCustomExceptionHandlerGemini {

    private static final String DAILY_QUOTA_BODY = """
            {"error":{"code":429,"status":"RESOURCE_EXHAUSTED","details":[
              {"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[
                {"quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier"}]},
              {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"14s"}]}}""";

    private final CustomExceptionHandler underTest = new CustomExceptionHandler();

    @Test
    void testQuotaExceededReturnsTooManyRequestsWithDetails() {
        ResponseEntity<ChatbotQuotaErrorDto> response = this.underTest.handleGeminiApiException(
                new GeminiApiException(HttpStatus.TOO_MANY_REQUESTS, DAILY_QUOTA_BODY));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("14", response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));

        ChatbotQuotaErrorDto body = response.getBody();
        assertNotNull(body);
        assertEquals("QUOTA_EXCEEDED", body.getReason());
        assertEquals("DAY", body.getQuotaScope());
        assertEquals(14, body.getRetryAfterSeconds());
    }

    /**
     * Le front doit rester capable d'afficher un message neutre quand Google ne dit rien
     * d'exploitable — c'est le cas qui survient si le format du corps change.
     */
    @Test
    void testQuotaExceededWithUnparseableBodyStillReturnsTooManyRequests() {
        ResponseEntity<ChatbotQuotaErrorDto> response = this.underTest.handleGeminiApiException(
                new GeminiApiException(HttpStatus.TOO_MANY_REQUESTS, "quelque chose d'inattendu"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNull(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER),
                "aucun Retry-After ne doit être inventé");

        ChatbotQuotaErrorDto body = response.getBody();
        assertNotNull(body);
        assertEquals("QUOTA_EXCEEDED", body.getReason());
        assertEquals("UNKNOWN", body.getQuotaScope());
        assertNull(body.getRetryAfterSeconds());
    }

    /**
     * Une panne Gemini qui n'est pas un quota est une défaillance amont : 502, et surtout pas le 500
     * indifférencié qui la confondrait avec un bug de l'application.
     */
    @Test
    void testNonQuotaFailureReturnsBadGateway() {
        ResponseEntity<ChatbotQuotaErrorDto> response = this.underTest.handleGeminiApiException(
                new GeminiApiException(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":{\"code\":500}}"));

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testBadRequestFromGeminiIsAlsoBadGateway() {
        ResponseEntity<ChatbotQuotaErrorDto> response = this.underTest.handleGeminiApiException(
                new GeminiApiException(HttpStatus.BAD_REQUEST, "{\"error\":{\"status\":\"INVALID_ARGUMENT\"}}"));

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }

}
