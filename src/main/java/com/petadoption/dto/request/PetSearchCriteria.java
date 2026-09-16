package com.petadoption.dto.request;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;

public record PetSearchCriteria(

        String species,

        EnergyLevel energyLevel,

        Temperament temperament,

        Integer minAge,

        Integer maxAge,

        PetStatus status,

        String search
) {
}
