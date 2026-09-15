package com.petadoption.controller;

import com.petadoption.dto.response.DashboardReportDto;
import com.petadoption.security.JwtAuthenticationFilter;
import com.petadoption.service.ReportService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetDashboardReport() throws Exception {

        when(reportService.getDashboardReport())
                .thenReturn(
                        org.mockito.Mockito.mock(
                                DashboardReportDto.class));

        mockMvc.perform(
                        get("/api/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Dashboard report generated successfully"));
    }
}