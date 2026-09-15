package com.petadoption.service.impl;

import com.petadoption.entity.AuditLog;
import com.petadoption.repository.AuditLogRepository;
import com.petadoption.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl
        implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void saveAuditLog(
            String action,
            String entityType,
            String entityId,
            String userEmail,
            String details) {

        AuditLog log = new AuditLog();

        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setUserEmail(userEmail);
        log.setDetails(details);

        auditLogRepository.save(log);
    }
}
