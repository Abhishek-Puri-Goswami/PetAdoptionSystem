package com.petadoption.dto.response;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;

import java.util.List;

public record PetResponseDto(

        Long id,

        String name,

        String species,

        String breed,

        Integer age,

        String gender,

        String description,

        PetStatus status,

        String imageUrl,

        EnergyLevel energyLevel,

        Temperament temperament,

        boolean sterilized,

        String specialCareNotes,

        List<String> imageUrls,

        Long shelterId,

        String shelterName
) {
}
