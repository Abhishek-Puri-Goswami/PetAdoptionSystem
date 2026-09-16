package com.petadoption.exception;

import com.petadoption.dto.response.ErrorResponseDto;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void shouldHandleAiServiceException() {

        AiServiceException exception =
                new AiServiceException(
                        "AI service is currently unavailable. "
                                + "Please try again later."
                );

        ResponseEntity<ErrorResponseDto> response =
                handler.handleAiServiceException(
                        exception
                );

        assertEquals(
                503,
                response.getStatusCode().value()
        );

        assertEquals(
                "AI Service Unavailable",
                response.getBody().getError()
        );

        assertEquals(
                "AI service is currently unavailable. "
                        + "Please try again later.",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleNoResourceFoundException() {

        NoResourceFoundException exception =
                mock(NoResourceFoundException.class);

        when(exception.getHttpMethod())
                .thenReturn(HttpMethod.GET);

        when(exception.getResourcePath())
                .thenReturn("api/does-not-exist");

        ResponseEntity<ErrorResponseDto> response =
                handler.handleNoResourceFound(exception);

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertEquals(
                "Not Found",
                response.getBody().getError()
        );

        assertTrue(
                response.getBody().getMessage()
                        .contains("api/does-not-exist")
        );
    }

    @Test
    void shouldHandleMalformedRequestBodyWithCauseMessage() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        RuntimeException cause = new RuntimeException(
                "Cannot deserialize value of type EnergyLevel "
                        + "from String \"SUPERHIGH\"");

        when(exception.getMostSpecificCause())
                .thenReturn(cause);

        ResponseEntity<ErrorResponseDto> response =
                handler.handleMalformedRequestBody(exception);

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Malformed Request",
                response.getBody().getError()
        );

        assertEquals(
                cause.getMessage(),
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleMalformedRequestBodyWithNoCause() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        when(exception.getMostSpecificCause())
                .thenReturn(null);

        ResponseEntity<ErrorResponseDto> response =
                handler.handleMalformedRequestBody(exception);

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "The request body is missing or not valid JSON",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleTypeMismatchException() {

        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName())
                .thenReturn("id");

        ResponseEntity<ErrorResponseDto> response =
                handler.handleTypeMismatch(exception);

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Malformed Request",
                response.getBody().getError()
        );

        assertEquals(
                "Invalid value for parameter 'id'",
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