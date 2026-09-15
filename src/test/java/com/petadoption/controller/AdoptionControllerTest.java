package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.AdoptionService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdoptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdoptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @MockitoBean
    private AdoptionService adoptionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreateApplication() throws Exception {

        AdoptionRequestDto request =
                new AdoptionRequestDto(
                        1L,
                        "Interested in adoption"
                );

        when(adoptionService.createApplication(any()))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AdoptionResponseDto.class));

        mockMvc.perform(
                        post("/api/adoptions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }

    @Test
    void shouldGetAllApplications() throws Exception {

        when(adoptionService.getAllApplications())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/adoptions"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMyApplications() throws Exception {

        when(adoptionService.getMyApplications())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/adoptions/my"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMyApplicationById() throws Exception {

        when(adoptionService.getMyApplicationById(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AdoptionResponseDto.class));

        mockMvc.perform(get("/api/adoptions/my/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetApplicationById() throws Exception {

        when(adoptionService.getApplicationById(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AdoptionResponseDto.class));

        mockMvc.perform(get("/api/adoptions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldApproveApplication() throws Exception {

        when(adoptionService.approveApplication(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AdoptionResponseDto.class));

        mockMvc.perform(
                        put("/api/adoptions/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectApplication() throws Exception {

        when(adoptionService.rejectApplication(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AdoptionResponseDto.class));

        mockMvc.perform(
                        put("/api/adoptions/1/reject"))
                .andExpect(status().isOk());
    }
}