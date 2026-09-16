package com.petadoption.dto.request;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.Temperament;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PetRequestDto(

        @NotBlank
        String name,

        String species,

        String breed,

        @PositiveOrZero
        Integer age,

        String gender,

        String description,

        EnergyLevel energyLevel,

        Temperament temperament,

        boolean sterilized,

        String specialCareNotes
) {
}
