package com.petadoption.exception;

import com.petadoption.dto.response.ErrorResponseDto;

import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleAccessDeniedException() {

        AccessDeniedException exception =
                new AccessDeniedException(
                        "Access denied"
                );

        ResponseEntity<ErrorResponseDto> response =
                handler.handleAccessDenied(
                        exception
                );

        assertEquals(
                403,
                response.getStatusCode().value()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                "Access Denied",
                response.getBody().getError()
        );

        assertEquals(
                "Access denied",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleResourceNotFoundException() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Pet not found"
                );

        ResponseEntity<ErrorResponseDto> response =
                handler.handleNotFound(
                        exception
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertEquals(
                "Not Found",
                response.getBody().getError()
        );

        assertEquals(
                "Pet not found",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleBusinessException() {

        BusinessException exception =
                new BusinessException(
                        "Business rule violated"
                );

        ResponseEntity<ErrorResponseDto> response =
                handler.handleBusinessException(
                        exception
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Business Error",
                response.getBody().getError()
        );

        assertEquals(
                "Business rule violated",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleGenericException() {

        Exception exception =
                new Exception(
                        "Unexpected error"
                );

        ResponseEntity<ErrorResponseDto> response =
                handler.handleGenericException(
                        exception
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertEquals(
                "Internal Server Error",
                response.getBody().getError()
        );

        assertEquals(
                "Unexpected error",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldPopulateTimestamp() {

        Exception exception =
                new Exception(
                        "Error"
                );

        ResponseEntity<ErrorResponseDto> response =
                handler.handleGenericException(
                        exception
                );

        assertNotNull(
                response.getBody()
                        .getTimestamp()
        );
    }
}