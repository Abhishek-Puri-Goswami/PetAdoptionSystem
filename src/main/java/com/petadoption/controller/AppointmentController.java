package com.petadoption.controller;

import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.AppointmentResponseDto;
import com.petadoption.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('ADOPTER')")
    @PostMapping
    public ApiResponseDto<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody AppointmentRequestDto requestDto) {

        return new ApiResponseDto<>(
                true,
                "Appointment created successfully",
                appointmentService.createAppointment(requestDto)
        );
    }

    @PreAuthorize("hasRole('ADOPTER')")
    @GetMapping("/my")
    public ApiResponseDto<List<AppointmentResponseDto>>
    getMyAppointments() {

        return new ApiResponseDto<>(
                true,
                "Appointments fetched successfully",
                appointmentService.getMyAppointments()
        );
    }

    @PreAuthorize("hasRole('ADOPTER')")
    @GetMapping("/my/{id}")
    public ApiResponseDto<AppointmentResponseDto>
    getMyAppointmentById(@PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Appointment fetched successfully",
                appointmentService.getMyAppointmentById(id)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping("/{id}")
    public ApiResponseDto<AppointmentResponseDto> getAppointmentById(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Appointment fetched successfully",
                appointmentService.getAppointmentById(id)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping
    public ApiResponseDto<List<AppointmentResponseDto>>
    getAllAppointments() {

        return new ApiResponseDto<>(
                true,
                "Appointments fetched successfully",
                appointmentService.getAllAppointments()
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN')")
    @PutMapping("/{id}/approve")
    public ApiResponseDto<AppointmentResponseDto> approve(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Appointment approved successfully",
                appointmentService.approveAppointment(id)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN')")
    @PutMapping("/{id}/reject")
    public ApiResponseDto<AppointmentResponseDto> reject(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Appointment rejected successfully",
                appointmentService.rejectAppointment(id)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN')")
    @PutMapping("/{id}/complete")
    public ApiResponseDto<AppointmentResponseDto> complete(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Appointment completed successfully",
                appointmentService.completeAppointment(id)
        );
    }
}