package com.petadoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.AppointmentResponseDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.AppointmentService;

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
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .findAndRegisterModules();

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreateAppointment() throws Exception {

        AppointmentRequestDto request =
                new AppointmentRequestDto(
                        1L,
                        1L,
                        LocalDateTime.now().plusDays(1),
                        "Visit pet"
                );

        when(appointmentService.createAppointment(any()))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AppointmentResponseDto.class));

        mockMvc.perform(
                        post("/api/appointments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMyAppointments() throws Exception {

        when(appointmentService.getMyAppointments())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/my"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMyAppointmentById() throws Exception {

        when(appointmentService.getMyAppointmentById(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AppointmentResponseDto.class));

        mockMvc.perform(get("/api/appointments/my/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAppointmentById() throws Exception {

        when(appointmentService.getAppointmentById(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AppointmentResponseDto.class));

        mockMvc.perform(get("/api/appointments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAllAppointments() throws Exception {

        when(appointmentService.getAllAppointments())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldApproveAppointment() throws Exception {

        when(appointmentService.approveAppointment(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AppointmentResponseDto.class));

        mockMvc.perform(put("/api/appointments/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAppointment() throws Exception {

        when(appointmentService.rejectAppointment(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AppointmentResponseDto.class));

        mockMvc.perform(put("/api/appointments/1/reject"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCompleteAppointment() throws Exception {

        when(appointmentService.completeAppointment(1L))
                .thenReturn(
                        org.mockito.Mockito.mock(
                                AppointmentResponseDto.class));

        mockMvc.perform(put("/api/appointments/1/complete"))
                .andExpect(status().isOk());
    }
}