package org.mosqueethonon.configuration.exception;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.exception.ForbiddenResourceAccessException;
import org.mosqueethonon.exception.ResourceLockedException;
import org.mosqueethonon.v1.dto.lock.LockResultDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("An unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
