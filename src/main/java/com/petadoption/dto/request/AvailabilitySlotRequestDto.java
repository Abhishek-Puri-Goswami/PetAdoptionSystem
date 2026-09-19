package com.petadoption.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailabilitySlotRequestDto(

        @NotNull(message = "Slot date and time is required")
        @Future(message = "Slot date and time must be in the future")
        LocalDateTime slotDateTime
) {
}
