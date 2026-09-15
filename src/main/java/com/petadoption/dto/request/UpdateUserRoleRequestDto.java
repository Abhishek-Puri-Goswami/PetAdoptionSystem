package com.petadoption.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
public record UpdateUserRoleRequestDto(

        @NotEmpty
        Set<String> roles
) {
}