package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.Temperament;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.PetService;

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

@WebMvcTest(PetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetAllPets() throws Exception {

        when(petService.getAllPets(any()))
                .thenReturn(
                        new PageResponseDto<>(
                                List.of(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }

    @Test
    void shouldGetPetById() throws Exception {

        PetResponseDto response =
                org.mockito.Mockito.mock(
                        PetResponseDto.class);

        when(petService.getPetById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/pets/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreatePet() throws Exception {

        PetRequestDto request =
                new PetRequestDto(
                        "Buddy",
                        "Dog",
                        "Labrador",
                        2,
                        "Male",
                        "Friendly dog",
                        EnergyLevel.MEDIUM,
                        Temperament.PLAYFUL
                );

        when(petService.createPet(any()))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                PetResponseDto.class));

        mockMvc.perform(
                        post("/api/pets")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdatePet() throws Exception {

        PetRequestDto request =
                new PetRequestDto(
                        "Buddy",
                        "Dog",
                        "Labrador",
                        2,
                        "Male",
                        "Updated description",
                        EnergyLevel.LOW,
                        Temperament.CALM
                );

        when(petService.updatePet(
                any(),
                any()))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                PetResponseDto.class));

        mockMvc.perform(
                        put("/api/pets/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeletePet() throws Exception {

        doNothing()
                .when(petService)
                .deletePet(1L);

        mockMvc.perform(
                        delete("/api/pets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }
}