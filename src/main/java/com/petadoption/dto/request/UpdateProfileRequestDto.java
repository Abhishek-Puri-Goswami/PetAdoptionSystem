package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record UpdateProfileRequestDto(

        @NotBlank(message = "First name is required")
        @Size(max = Rules.NAME_MAX, message = Rules.NAME_MESSAGE)
        @Pattern(regexp = Rules.NAME_REGEX, message = Rules.NAME_MESSAGE)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = Rules.NAME_MAX, message = Rules.NAME_MESSAGE)
        @Pattern(regexp = Rules.NAME_REGEX, message = Rules.NAME_MESSAGE)
        String lastName,

        @Pattern(regexp = Rules.PHONE_REGEX, message = Rules.PHONE_MESSAGE)
        String phone,

        @Size(max = Rules.ADDRESS_LINE_MAX,
                message = "Address line 1 must be at most 100 characters")
        String addressLine1,

        @Size(max = Rules.ADDRESS_LINE_MAX,
                message = "Address line 2 must be at most 100 characters")
        String addressLine2,

        @Size(max = Rules.PLACE_MAX,
                message = "City must be at most 60 characters")
        String city,

        @Size(max = Rules.PLACE_MAX,
                message = "State must be at most 60 characters")
        String state,

        @Pattern(regexp = Rules.POSTAL_REGEX, message = Rules.POSTAL_MESSAGE)
        String postalCode,

        @Size(max = Rules.PLACE_MAX,
                message = "Country must be at most 60 characters")
        String country
) {
}
