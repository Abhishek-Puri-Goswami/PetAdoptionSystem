package com.petadoption.dto.request;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.Temperament;
import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record PetRequestDto(

        @NotBlank(message = "Pet name is required")
        @Size(max = Rules.PET_NAME_MAX,
                message = "Pet name must be at most 60 characters")
        String name,

        @NotBlank(message = "Species is required")
        @Pattern(regexp = Rules.SPECIES_REGEX, message = Rules.SPECIES_MESSAGE)
        String species,

        @Size(max = Rules.PET_BREED_MAX,
                message = "Breed must be at most 60 characters")
        String breed,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be between 0 and 40")
        @Max(value = Rules.PET_AGE_MAX, message = "Age must be between 0 and 40")
        Integer age,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = Rules.GENDER_REGEX, message = Rules.GENDER_MESSAGE)
        String gender,

        @Size(max = Rules.DESCRIPTION_MAX,
                message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Energy level is required")
        EnergyLevel energyLevel,

        @NotNull(message = "Temperament is required")
        Temperament temperament,

        boolean sterilized,

        @Size(max = Rules.NOTES_MAX,
                message = "Special care notes must be at most 500 characters")
        String specialCareNotes
) {
}
