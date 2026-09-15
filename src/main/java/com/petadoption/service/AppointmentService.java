package com.petadoption.service;

import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.AppointmentResponseDto;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDto createAppointment(AppointmentRequestDto request);

    AppointmentResponseDto getAppointmentById(Long id);

    List<AppointmentResponseDto> getAllAppointments();

    List<AppointmentResponseDto> getMyAppointments();

    AppointmentResponseDto getMyAppointmentById(Long id);

    AppointmentResponseDto approveAppointment(Long id);

    AppointmentResponseDto rejectAppointment(Long id);

    AppointmentResponseDto completeAppointment(Long id);
}