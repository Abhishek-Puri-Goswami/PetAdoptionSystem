package com.petadoption.dto.request;

import jakarta.validation.constraints.Positive;

// shelterId may be null: null unassigns the user from any shelter.
public record AssignShelterRequestDto(

        @Positive(message = "Shelter id must be positive")
        Long shelterId
) {
}
