package com.petadoption.dto.request;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequestDto(

        @NotNull
        Long petId,

        @NotNull
        Long slotId,

        String notes
) {
}
