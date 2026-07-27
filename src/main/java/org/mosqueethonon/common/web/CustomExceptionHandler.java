package org.mosqueethonon.common.web;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.common.exception.BadRequestException;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.common.exception.ForbiddenResourceAccessException;
import org.mosqueethonon.chatbot.exception.GeminiApiException;
import org.mosqueethonon.chatbot.exception.GeminiQuotaDetails;
import org.mosqueethonon.lock.exception.ResourceLockedException;
import org.mosqueethonon.chatbot.v1.dto.ChatbotQuotaErrorDto;
import org.mosqueethonon.lock.v1.dto.LockResultDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        log.error("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(ForbiddenResourceAccessException.class)
    public ResponseEntity<String> handleForbiddenResourceAccessException(ForbiddenResourceAccessException e) {
        log.error("Forbidden resource access: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(ResourceLockedException.class)
    public ResponseEntity<LockResultDto> handleResourceLockedException(ResourceLockedException e) {
        log.error("Resource locked: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getLockResult());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Un dépassement de quota de l'API d'IA est une condition attendue en offre gratuite, pas un
     * incident : on renvoie 429 avec de quoi permettre au front de formuler un message utile, plutôt
     * qu'un 500 indifférencié. Toute autre défaillance Gemini devient un 502, car elle est le fait
     * d'un service amont et non d'un bug de l'application.
     */
    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ChatbotQuotaErrorDto> handleGeminiApiException(GeminiApiException e) {
        if (!e.isQuotaExceeded()) {
            log.error("Appel à l'API Gemini en échec (HTTP {})", e.getStatus().value(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        GeminiQuotaDetails details = e.getQuotaDetails();
        log.warn("Quota de l'API Gemini dépassé (portée={}, délai suggéré={}s)",
                details.scope(), details.retryAfterSeconds().isPresent()
                        ? details.retryAfterSeconds().getAsInt() : "inconnu");

        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        details.retryAfterSeconds().ifPresent(seconds ->
                response.header(HttpHeaders.RETRY_AFTER, String.valueOf(seconds)));

        return response.body(ChatbotQuotaErrorDto.builder()
                .reason("QUOTA_EXCEEDED")
                .quotaScope(details.scope().name())
                .retryAfterSeconds(details.retryAfterSeconds().isPresent()
                        ? details.retryAfterSeconds().getAsInt() : null)
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("An unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
