package com.petadoption.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ShelterRequestDto(

        @NotBlank
        String name,

        String email,

        String phone,

        String addressLine1,

        String addressLine2,

        String city,

        String state,

        String postalCode,

        String country,

        String description
) {
}