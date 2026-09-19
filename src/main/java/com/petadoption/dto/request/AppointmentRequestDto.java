package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record AppointmentRequestDto(

        @NotNull(message = "Pet id is required")
        @Positive(message = "Pet id must be positive")
        Long petId,

        @NotNull(message = "Slot id is required")
        @Positive(message = "Slot id must be positive")
        Long slotId,

        @Size(max = Rules.NOTES_MAX,
                message = "Notes must be at most 500 characters")
        String notes
) {
}
