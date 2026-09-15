package com.petadoption.dto.response;

import com.petadoption.enums.PetStatus;

public record PetResponseDto(

        Long id,

        String name,

        String species,

        String breed,

        Integer age,

        String gender,

        String description,

        PetStatus status
) {
}