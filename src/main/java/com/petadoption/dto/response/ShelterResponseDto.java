package com.petadoption.dto.response;

public record ShelterResponseDto(

        Long id,

        String name,

        String email,

        String phone,

        String city,

        String state,

        String country,

        String description
) {
}