package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record LoginRequestDto(

        @NotBlank(message = "Email is required")
        @Size(max = Rules.EMAIL_MAX, message = Rules.EMAIL_MESSAGE)
        String email,

        // Login checks presence and length only, never the password
        // policy, so accounts created under older rules can still sign in.
        @NotBlank(message = "Password is required")
        @Size(max = Rules.PASSWORD_MAX,
                message = "Password must be at most 64 characters")
        String password
) {
}
