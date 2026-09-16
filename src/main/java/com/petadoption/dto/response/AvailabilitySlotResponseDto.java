package com.petadoption.dto.response;

import java.time.LocalDateTime;

public record AvailabilitySlotResponseDto(

        Long id,

        LocalDateTime slotDateTime,

        boolean booked,

        Long shelterId,

        String shelterName
) {
}
