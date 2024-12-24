package org.mosqueethonon.configuration.exception;

import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        log.error("An unexpected error occurred ", e);
        if(e instanceof ResourceNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Resource not found ! " + e.getMessage());
        } else if (e instanceof BadRequestException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad request ! " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
    }

}
