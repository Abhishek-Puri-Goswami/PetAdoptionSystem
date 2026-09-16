package com.petadoption.controller;

import com.petadoption.dto.request.MedicalRecordRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.MedicalRecordResponseDto;
import com.petadoption.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets/{petId}/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN','SHELTER_STAFF')")
    @PostMapping
    public ApiResponseDto<MedicalRecordResponseDto> createRecord(
            @PathVariable Long petId,
            @Valid @RequestBody MedicalRecordRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Medical record created successfully",
                medicalRecordService.createRecord(petId, request)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping
    public ApiResponseDto<List<MedicalRecordResponseDto>> getRecordsForPet(
            @PathVariable Long petId) {

        return new ApiResponseDto<>(
                true,
                "Medical records fetched successfully",
                medicalRecordService.getRecordsForPet(petId)
        );
    }
}
