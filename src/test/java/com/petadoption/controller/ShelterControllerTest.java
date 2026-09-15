package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.ShelterService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShelterController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShelterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @MockitoBean
    private ShelterService shelterService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetAllShelters() throws Exception {

        when(shelterService.getAllShelters())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/shelters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }

    @Test
    void shouldGetShelterById() throws Exception {

        when(shelterService.getShelterById(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                ShelterResponseDto.class));

        mockMvc.perform(get("/api/shelters/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateShelter() throws Exception {

        ShelterRequestDto request =
                new ShelterRequestDto(
                        "Happy Paws Shelter",
                        "shelter@test.com",
                        "9876543210",
                        "Address Line 1",
                        "Address Line 2",
                        "Bangalore",
                        "Karnataka",
                        "560001",
                        "India",
                        "Animal rescue center"
                );

        when(shelterService.createShelter(any()))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                ShelterResponseDto.class));

        mockMvc.perform(
                        post("/api/shelters")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateShelter() throws Exception {

        ShelterRequestDto request =
                new ShelterRequestDto(
                        "Updated Shelter",
                        "updated@test.com",
                        "9876543210",
                        "Address 1",
                        "Address 2",
                        "Bangalore",
                        "Karnataka",
                        "560001",
                        "India",
                        "Updated description"
                );

        when(shelterService.updateShelter(
                any(),
                any()))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                ShelterResponseDto.class));

        mockMvc.perform(
                        put("/api/shelters/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteShelter() throws Exception {

        doNothing()
                .when(shelterService)
                .deleteShelter(1L);

        mockMvc.perform(
                        delete("/api/shelters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }
}