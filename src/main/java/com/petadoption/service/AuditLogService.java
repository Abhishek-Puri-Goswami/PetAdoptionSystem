package com.petadoption.service;

public interface AuditLogService {

    void saveAuditLog(
            String action,
            String entityType,
            String entityId,
            String userEmail,
            String details);
}