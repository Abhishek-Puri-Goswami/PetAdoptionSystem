package com.petadoption.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MedicalRecordRequestDto(

        @NotNull
        LocalDate recordDate,

        String description,

        String vetName
) {
}
