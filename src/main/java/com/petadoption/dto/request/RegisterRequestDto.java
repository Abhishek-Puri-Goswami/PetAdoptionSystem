package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record RegisterRequestDto(

        @NotBlank(message = "First name is required")
        @Size(max = Rules.NAME_MAX, message = Rules.NAME_MESSAGE)
        @Pattern(regexp = Rules.NAME_REGEX, message = Rules.NAME_MESSAGE)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = Rules.NAME_MAX, message = Rules.NAME_MESSAGE)
        @Pattern(regexp = Rules.NAME_REGEX, message = Rules.NAME_MESSAGE)
        String lastName,

        @NotBlank(message = "Email is required")
        @Size(max = Rules.EMAIL_MAX, message = Rules.EMAIL_MESSAGE)
        @Pattern(regexp = Rules.EMAIL_REGEX, message = Rules.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = Rules.PASSWORD_REGEX, message = Rules.PASSWORD_MESSAGE)
        String password
) {
}
