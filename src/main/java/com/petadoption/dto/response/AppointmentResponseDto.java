package com.petadoption.dto.response;

import java.time.LocalDateTime;

public record AppointmentResponseDto(

        Long id,

        Long petId,

        Long adopterId,

        Long shelterId,

        LocalDateTime appointmentDateTime,

        String notes,

        String status
) {
}