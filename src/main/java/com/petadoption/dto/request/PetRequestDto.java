package com.petadoption.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PetRequestDto(

        @NotBlank
        String name,

        String species,

        String breed,

        @PositiveOrZero
        Integer age,

        String gender,

        String description
) {
}