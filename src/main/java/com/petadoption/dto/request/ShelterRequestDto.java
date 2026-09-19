package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record ShelterRequestDto(

        @NotBlank(message = "Shelter name is required")
        @Size(min = 2, max = Rules.SHELTER_NAME_MAX,
                message = "Shelter name must be 2-100 characters")
        String name,

        @NotBlank(message = "Shelter email is required")
        @Size(max = Rules.EMAIL_MAX, message = Rules.EMAIL_MESSAGE)
        @Pattern(regexp = Rules.EMAIL_REGEX, message = Rules.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Shelter phone is required")
        @Pattern(regexp = Rules.PHONE_REGEX, message = Rules.PHONE_MESSAGE)
        String phone,

        @Size(max = Rules.ADDRESS_LINE_MAX,
                message = "Address line 1 must be at most 100 characters")
        String addressLine1,

        @Size(max = Rules.ADDRESS_LINE_MAX,
                message = "Address line 2 must be at most 100 characters")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = Rules.PLACE_MAX,
                message = "City must be at most 60 characters")
        String city,

        @Size(max = Rules.PLACE_MAX,
                message = "State must be at most 60 characters")
        String state,

        @Pattern(regexp = Rules.POSTAL_REGEX, message = Rules.POSTAL_MESSAGE)
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(max = Rules.PLACE_MAX,
                message = "Country must be at most 60 characters")
        String country,

        @Size(max = Rules.DESCRIPTION_MAX,
                message = "Description must be at most 1000 characters")
        String description
) {
}
