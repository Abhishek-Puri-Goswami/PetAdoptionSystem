package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record AdoptionRequestDto(

        @NotNull(message = "Pet id is required")
        @Positive(message = "Pet id must be positive")
        Long petId,

        @Size(max = Rules.APPLICANT_NOTES_MAX,
                message = "Applicant notes must be at most 1000 characters")
        String applicantNotes,

        @NotBlank(message = "Living situation is required")
        @Size(max = Rules.NOTES_MAX,
                message = "Living situation must be at most 500 characters")
        String livingSituation,

        @Size(max = Rules.NOTES_MAX,
                message = "Prior pet experience must be at most 500 characters")
        String priorPetExperience,

        @Size(max = Rules.NOTES_MAX,
                message = "Household details must be at most 500 characters")
        String householdDetails,

        @NotBlank(message = "Preferred contact is required")
        @Size(max = Rules.PREFERRED_CONTACT_MAX,
                message = "Preferred contact must be at most 100 characters")
        String preferredContact
) {
}
