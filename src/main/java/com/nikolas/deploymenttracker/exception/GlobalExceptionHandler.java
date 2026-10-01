
package com.nikolas.deploymenttracker.exception;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import java.sql.SQLException;

import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");

        Map<String, Object> response = Map.of(
                "status", 400,
                "error", "Validation Failed",
                "message", message,
                "path", request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

@ExceptionHandler(NoSuchElementException.class)
public ResponseEntity<Map<String, Object>> handleNotFoundException(
        NoSuchElementException exception,
        HttpServletRequest request) {

    Map<String, Object> response = Map.of(
            "status", 404,
            "error", "Not Found",
            "message", exception.getMessage(),
            "path", request.getRequestURI()
    );

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response);
}


@ExceptionHandler(DuplicateApplicationException.class)
public ResponseEntity<Map<String, Object>> handleDuplicateApplication(
        DuplicateApplicationException exception,
        HttpServletRequest request) {

    Map<String, Object> response = Map.of(
            "status", 409,
            "error", "Conflict",
            "message", exception.getMessage(),
            "path", request.getRequestURI()
    );

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(response);
}


@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<Map<String, Object>> handleInvalidJson(
        HttpMessageNotReadableException exception,
        HttpServletRequest request) {

    Map<String, Object> response = Map.of(
            "status", 400,
            "error", "Bad Request",
            "message", "Malformed JSON or unsupported field value",
            "path", request.getRequestURI()
    );

    return ResponseEntity
            .badRequest()
            .body(response);
}

@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
        DataIntegrityViolationException exception,
        HttpServletRequest request) {

    Throwable cause = exception;

    while (cause != null) {

        if (cause instanceof SQLException sqlException
                && "23505".equals(sqlException.getSQLState())) {

            Map<String, Object> response = Map.of(
                    "status", 409,
                    "error", "Conflict",
                    "message", "A resource with these unique values already exists",
                    "path", request.getRequestURI()
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        cause = cause.getCause();
    }

    Map<String, Object> response = Map.of(
            "status", 500,
            "error", "Internal Server Error",
            "message", "Database operation failed",
            "path", request.getRequestURI()
    );

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
}


}
