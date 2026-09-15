package com.petadoption.controller;

import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.service.AdoptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    @PostMapping
    public ApiResponseDto<AdoptionResponseDto> createApplication(
            @Valid @RequestBody AdoptionRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Application submitted successfully",
                adoptionService.createApplication(request)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_STAFF','SHELTER_ADMIN','SYSTEM_ADMIN')"
    )
    @GetMapping
    public ApiResponseDto<List<AdoptionResponseDto>>
    getAllApplications() {

        return new ApiResponseDto<>(
                true,
                "Applications fetched successfully",
                adoptionService.getAllApplications()
        );
    }

    @PreAuthorize("hasRole('ADOPTER')")
    @GetMapping("/my")
    public ApiResponseDto<List<AdoptionResponseDto>>
    getMyApplications() {

        return new ApiResponseDto<>(
                true,
                "Applications fetched successfully",
                adoptionService.getMyApplications()
        );
    }

    @PreAuthorize("hasRole('ADOPTER')")
    @GetMapping("/my/{id}")
    public ApiResponseDto<AdoptionResponseDto>
    getMyApplicationById(@PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Application fetched successfully",
                adoptionService.getMyApplicationById(id)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN','SYSTEM_ADMIN')"
    )
    @GetMapping("/{id}")
    public ApiResponseDto<AdoptionResponseDto>
    getApplicationById(@PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Application fetched successfully",
                adoptionService.getApplicationById(id)
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PutMapping("/{id}/approve")
    public ApiResponseDto<AdoptionResponseDto>
    approveApplication(@PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Application approved successfully",
                adoptionService.approveApplication(id)
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PutMapping("/{id}/reject")
    public ApiResponseDto<AdoptionResponseDto>
    rejectApplication(@PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Application rejected successfully",
                adoptionService.rejectApplication(id)
        );
    }
}