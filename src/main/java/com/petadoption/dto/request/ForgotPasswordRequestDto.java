package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record ForgotPasswordRequestDto(

        @NotBlank(message = "Email is required")
        @Size(max = Rules.EMAIL_MAX, message = Rules.EMAIL_MESSAGE)
        @Pattern(regexp = Rules.EMAIL_REGEX, message = Rules.EMAIL_MESSAGE)
        String email
) {
}
