package com.petadoption.dto.response;

import java.util.Set;

public record UserResponseDto(

        Long id,

        String firstName,

        String lastName,

        String email,

        boolean enabled,

        Set<String> roles,

        String phone,

        String addressLine1,

        String addressLine2,

        String city,

        String state,

        String postalCode,

        String country,

        Long shelterId,

        String shelterName
) {
}