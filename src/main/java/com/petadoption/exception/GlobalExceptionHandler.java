package com.petadoption.exception;

import com.petadoption.dto.response.ErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Access Denied")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessException(
            BusinessException ex) {

        return ResponseEntity.badRequest()
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Business Error")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleAiServiceException(
            AiServiceException ex) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .error("AI Service Unavailable")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        // "roles[]" (a collection element) becomes just "roles" so a form
        // can match the error to its field.
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(
                        error.getField().replaceAll("\\[[^\\]]*\\]", ""),
                        error.getDefaultMessage()));

        String message = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return validationBody(message, fieldErrors);
    }

    // Query parameters validated with @Validated on a controller.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            fieldErrors.putIfAbsent(
                    path.substring(path.lastIndexOf('.') + 1),
                    violation.getMessage());
        });

        String message = fieldErrors.values().stream()
                .findFirst().orElse("Validation failed");

        return validationBody(message, fieldErrors);
    }

    private ResponseEntity<ErrorResponseDto> validationBody(
            String message, Map<String, String> fieldErrors) {

        return ResponseEntity.badRequest()
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Validation Error")
                        .message(message)
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(
            NoResourceFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .message("No endpoint found for "
                                + ex.getHttpMethod() + " "
                                + ex.getResourcePath())
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedRequestBody(
            HttpMessageNotReadableException ex) {

        Throwable cause = ex.getMostSpecificCause();

        String message = cause != null && cause.getMessage() != null
                ? cause.getMessage()
                : "The request body is missing or not valid JSON";

        return ResponseEntity.badRequest()
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Malformed Request")
                        .message(message)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        return ResponseEntity.badRequest()
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Malformed Request")
                        .message("Invalid value for parameter '"
                                + ex.getName() + "'")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex) {

        return ResponseEntity.internalServerError()
                .body(ErrorResponseDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .message(ex.getMessage())
                        .build());
    }
}