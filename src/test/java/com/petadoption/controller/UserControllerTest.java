package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserResponseDto buildUser() {

        return new UserResponseDto(
                1L,
                "Abhishek",
                "Goswami",
                "test@test.com",
                true,
                Set.of("ROLE_ADOPTER")
        );
    }

    @Test
    void shouldGetAllUsers() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of(buildUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.length()")
                        .value(1));
    }

    @Test
    void shouldGetUserById() throws Exception {

        when(userService.getUserById(1L))
                .thenReturn(buildUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.email")
                        .value("test@test.com"));
    }

    @Test
    void shouldUpdateRoles() throws Exception {

        UpdateUserRoleRequestDto request =
                new UpdateUserRoleRequestDto(
                        Set.of("ROLE_SHELTER_ADMIN")
                );

        when(userService.updateRoles(any(), any()))
                .thenReturn(buildUser());

        mockMvc.perform(
                        put("/api/users/1/roles")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Roles updated successfully"));
    }

    @Test
    void shouldDisableUser() throws Exception {

        when(userService.disableUser(1L))
                .thenReturn(buildUser());

        mockMvc.perform(
                        put("/api/users/1/disable"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "User disabled successfully"));
    }

    @Test
    void shouldEnableUser() throws Exception {

        when(userService.enableUser(1L))
                .thenReturn(buildUser());

        mockMvc.perform(
                        put("/api/users/1/enable"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "User enabled successfully"));
    }
}