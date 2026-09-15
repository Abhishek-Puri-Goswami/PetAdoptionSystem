package com.petadoption.dto.response;

public record AuditLogResponseDto(

        Long id,

        String action,

        String entityType,

        String entityId,

        String userEmail,

        String details
) {
}
