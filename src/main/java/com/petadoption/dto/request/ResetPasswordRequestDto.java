package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record ResetPasswordRequestDto(

        @NotBlank(message = "Reset token is required")
        @Size(max = Rules.TOKEN_MAX,
                message = "Reset token must be at most 100 characters")
        String token,

        @NotBlank(message = "New password is required")
        @Pattern(regexp = Rules.PASSWORD_REGEX, message = Rules.PASSWORD_MESSAGE)
        String newPassword
) {
}
