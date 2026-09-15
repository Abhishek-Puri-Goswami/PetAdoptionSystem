package com.petadoption.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequestDto(

        @NotNull
        Long petId,

        @NotNull
        Long shelterId,

        @Future
        LocalDateTime appointmentDateTime,

        String notes
) {
}