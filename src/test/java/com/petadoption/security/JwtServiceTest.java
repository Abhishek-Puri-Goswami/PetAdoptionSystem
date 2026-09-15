package com.petadoption.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Minimum 32+ bytes for HS256
    private static final String SECRET =
            "mysecretkeymysecretkeymysecretkey123456";

    @BeforeEach
    void setUp() {

        jwtService =
                new JwtService(
                        SECRET,
                        3600000L // 1 hour
                );
    }

    @Test
    void shouldGenerateToken() {

        String token =
                jwtService.generateToken(
                        "test@test.com"
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractEmailFromToken() {

        String token =
                jwtService.generateToken(
                        "test@test.com"
                );

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "test@test.com",
                email
        );
    }

    @Test
    void shouldValidateGeneratedToken() {

        String token =
                jwtService.generateToken(
                        "test@test.com"
                );

        assertTrue(
                jwtService.isTokenValid(token)
        );
    }

    @Test
    void shouldReturnFalseForInvalidToken() {

        assertFalse(
                jwtService.isTokenValid(
                        "invalid-token"
                )
        );
    }

    @Test
    void shouldReturnFalseForTamperedToken() {

        String token =
                jwtService.generateToken(
                        "test@test.com"
                );

        String tampered =
                token + "123";

        assertFalse(
                jwtService.isTokenValid(
                        tampered
                )
        );
    }

    @Test
    void shouldGenerateDifferentTokens() {

        String token1 =
                jwtService.generateToken(
                        "test@test.com"
                );

        String token2 =
                jwtService.generateToken(
                        "test2@test.com"
                );

        assertNotEquals(
                token1,
                token2
        );
    }
}