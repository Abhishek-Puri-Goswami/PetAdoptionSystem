package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record UpdateUserRoleRequestDto(

        @NotEmpty(message = "At least one role is required")
        Set<@Pattern(regexp = Rules.ROLE_REGEX,
                message = Rules.ROLE_MESSAGE) String> roles
) {
}
