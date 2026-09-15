package com.petadoption.controller;

import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.entity.AuditLog;
import com.petadoption.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ApiResponseDto<List<AuditLog>>
    getAllLogs() {

        return new ApiResponseDto<>(
                true,
                "Audit logs fetched successfully",
                auditLogRepository.findAll());
    }
}