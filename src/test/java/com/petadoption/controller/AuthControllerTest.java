package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.LoginRequestDto;
import com.petadoption.dto.request.RegisterRequestDto;
import com.petadoption.dto.response.AuthResponseDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldRegisterUser() throws Exception {

        RegisterRequestDto request =
                new RegisterRequestDto(
                        "Abhishek",
                        "Goswami",
                        "test@test.com",
                        "Password123"
                );

        AuthResponseDto response =
                AuthResponseDto.builder()
                        .userId(1L)
                        .email("test@test.com")
                        .token("jwt-token")
                        .build();

        when(authService.register(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "User registered successfully"))
                .andExpect(
                        jsonPath("$.data.email")
                                .value(
                                        "test@test.com"));
    }

    @Test
    void shouldLoginUser() throws Exception {

        LoginRequestDto request =
                new LoginRequestDto(
                        "test@test.com",
                        "Password123"
                );

        AuthResponseDto response =
                AuthResponseDto.builder()
                        .userId(1L)
                        .email("test@test.com")
                        .token("jwt-token")
                        .build();

        when(authService.login(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Login successful"))
                .andExpect(
                        jsonPath("$.data.token")
                                .value(
                                        "jwt-token"));
    }

    @Test
    void shouldFailValidationForRegister() throws Exception {

        RegisterRequestDto request =
                new RegisterRequestDto(
                        "",
                        "",
                        "invalid-email",
                        ""
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isBadRequest());
    }
}