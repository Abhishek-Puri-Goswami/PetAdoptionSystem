package com.petadoption.dto.response;

import java.time.LocalDateTime;

public record AppNotificationResponseDto(

        Long id,

        String message,

        boolean read,

        LocalDateTime createdAt
) {
}
