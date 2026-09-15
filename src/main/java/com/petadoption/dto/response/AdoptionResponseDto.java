package com.petadoption.dto.response;

import com.petadoption.enums.ApplicationStatus;

public record AdoptionResponseDto(

        Long id,

        Long petId,

        String petName,

        Long adopterId,

        String adopterEmail,

        String applicantNotes,

        ApplicationStatus status
) {
}