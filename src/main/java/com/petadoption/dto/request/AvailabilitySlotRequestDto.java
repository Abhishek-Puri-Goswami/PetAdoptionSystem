package com.petadoption.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailabilitySlotRequestDto(

        @NotNull
        @Future
        LocalDateTime slotDateTime
) {
}
