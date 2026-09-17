package com.petadoption.ai.tool;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.Temperament;

public record PetSummary(

        Long id,

        String name,

        String species,

        String breed,

        Integer age,

        String gender,

        EnergyLevel energyLevel,

        Temperament temperament,

        String description
) {
}
