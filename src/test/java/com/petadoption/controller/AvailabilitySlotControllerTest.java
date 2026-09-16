package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.AvailabilitySlotRequestDto;
import com.petadoption.dto.response.AvailabilitySlotResponseDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.AvailabilitySlotService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvailabilitySlotController.class)
@AutoConfigureMockMvc(addFilters = false)
class AvailabilitySlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .findAndRegisterModules();

    @MockitoBean
    private AvailabilitySlotService slotService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreateSlot() throws Exception {

        AvailabilitySlotRequestDto request =
                new AvailabilitySlotRequestDto(
                        LocalDateTime.now().plusDays(1));

        when(slotService.createSlot(any()))
                .thenReturn(mock(AvailabilitySlotResponseDto.class));

        mockMvc.perform(
                        post("/api/slots")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetOpenSlotsForShelter() throws Exception {

        when(slotService.getOpenSlotsForShelter(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/slots/shelter/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMySlots() throws Exception {

        when(slotService.getMySlots())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/slots/my"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteSlot() throws Exception {

        mockMvc.perform(delete("/api/slots/1"))
                .andExpect(status().isOk());
    }
}
