package com.petadoption.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(

        @NotBlank
        String message
) {
}
