package com.petadoption.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequestDto(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        String phone,

        String addressLine1,

        String addressLine2,

        String city,

        String state,

        String postalCode,

        String country
) {
}
