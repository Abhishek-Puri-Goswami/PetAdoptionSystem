package com.petadoption.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequestDto(

        // Every message becomes paid model input, so cap it.
        @NotBlank
        @Size(max = 1000, message = "Message must be at most 1000 characters")
        String message
) {
}
