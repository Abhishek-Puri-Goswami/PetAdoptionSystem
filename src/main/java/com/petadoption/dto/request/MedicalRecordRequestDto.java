package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MedicalRecordRequestDto(

        @NotNull(message = "Record date is required")
        @PastOrPresent(message = "Record date cannot be in the future")
        LocalDate recordDate,

        @NotBlank(message = "Description is required")
        @Size(max = Rules.DESCRIPTION_MAX,
                message = "Description must be at most 1000 characters")
        String description,

        @Size(max = Rules.VET_NAME_MAX,
                message = "Vet name must be at most 100 characters")
        String vetName
) {
}
