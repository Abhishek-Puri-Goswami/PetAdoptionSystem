package com.petadoption.controller;

import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.DashboardReportDto;
import com.petadoption.service.ReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/dashboard")
    public ApiResponseDto<DashboardReportDto>
    getDashboardReport() {

        return new ApiResponseDto<>(
                true,
                "Dashboard report generated successfully",
                reportService.getDashboardReport()
        );
    }
}