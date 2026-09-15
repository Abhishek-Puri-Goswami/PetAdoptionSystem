package com.petadoption.security;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationEntryPointTest {

    private final JwtAuthenticationEntryPoint
            jwtAuthenticationEntryPoint =
            new JwtAuthenticationEntryPoint();

    @Test
    void shouldReturnUnauthorizedResponse()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                new BadCredentialsException(
                        "Invalid credentials"
                );

        jwtAuthenticationEntryPoint.commence(
                request,
                response,
                exception
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertEquals(
                "application/json",
                response.getContentType()
        );

        String responseBody =
                response.getContentAsString();

        assertNotNull(responseBody);

        assertTrue(
                responseBody.contains(
                        "Unauthorized"
                )
        );

        assertTrue(
                responseBody.contains(
                        "Authentication is required"
                )
        );

        assertTrue(
                responseBody.contains(
                        "\"status\":401"
                )
        );
    }

    @Test
    void shouldWriteErrorResponseBody()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                new BadCredentialsException(
                        "Bad credentials"
                );

        jwtAuthenticationEntryPoint.commence(
                request,
                response,
                exception
        );

        String body =
                response.getContentAsString();

        assertFalse(
                body.isBlank()
        );

        assertTrue(
                body.contains(
                        "Unauthorized"
                )
        );
    }
}