package com.petadoption.security;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenNoAuthorizationHeader()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(
                        request,
                        response
                );
    }

    @Test
    void shouldContinueWhenHeaderIsNotBearer()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic xyz"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(
                        request,
                        response
                );
    }

    @Test
    void shouldRejectInvalidToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(jwtService.isTokenValid(
                "invalid-token"))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        verify(filterChain, never())
                .doFilter(any(), any());
    }

    @Test
    void shouldAuthenticateValidToken()
            throws Exception {

        String token =
                "valid-token";

        String email =
                "test@test.com";

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        UserDetails userDetails =
                new User(
                        email,
                        "password",
                        Collections.emptyList()
                );

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractEmail(token))
                .thenReturn(email);

        when(userDetailsService
                .loadUserByUsername(email))
                .thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(
                        request,
                        response
                );
    }

    @Test
    void shouldContinueIfAuthenticationAlreadyExists()
            throws Exception {

        String token =
                "token";

        String email =
                "test@test.com";

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        mock(org.springframework.security.core.Authentication.class)
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractEmail(token))
                .thenReturn(email);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(userDetailsService, never())
                .loadUserByUsername(any());

        verify(filterChain)
                .doFilter(
                        request,
                        response
                );
    }
}