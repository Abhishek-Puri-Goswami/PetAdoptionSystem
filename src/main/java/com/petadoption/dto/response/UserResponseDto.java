package com.petadoption.dto.response;

import java.util.Set;

public record UserResponseDto(

        Long id,

        String firstName,

        String lastName,

        String email,

        boolean enabled,

        Set<String> roles
) {
}