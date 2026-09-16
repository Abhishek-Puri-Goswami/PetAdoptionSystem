package com.petadoption.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdoptionRequestDto(

        @NotNull
        Long petId,

        String applicantNotes,

        String livingSituation,

        String priorPetExperience,

        String householdDetails,

        String preferredContact
) {
}
