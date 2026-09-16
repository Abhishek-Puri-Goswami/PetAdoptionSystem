package com.petadoption.dto.response;

import java.time.LocalDate;

public record MedicalRecordResponseDto(

        Long id,

        Long petId,

        LocalDate recordDate,

        String description,

        String vetName
) {
}
