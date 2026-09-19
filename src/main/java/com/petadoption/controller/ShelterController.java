package com.petadoption.controller;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.service.ShelterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import com.petadoption.validation.Rules;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/shelters")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping
    public ApiResponseDto<ShelterResponseDto> createShelter(
            @Valid @RequestBody ShelterRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Shelter created successfully",
                shelterService.createShelter(request)
        );
    }

    // Public (anonymous allowed): see SecurityConfig.
    @GetMapping
    public ApiResponseDto<PageResponseDto<ShelterResponseDto>>
    getAllShelters(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) @Size(max = Rules.SEARCH_MAX, message = "Name filter must be at most 100 characters") String name) {

        return new ApiResponseDto<>(
                true,
                "Shelters fetched successfully",
                shelterService.getAllShelters(pageable, name)
        );
    }

    // Public (anonymous allowed): see SecurityConfig.
    @GetMapping("/{id}")
    public ApiResponseDto<ShelterResponseDto> getShelterById(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Shelter fetched successfully",
                shelterService.getShelterById(id)
        );
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseDto<ShelterResponseDto> updateShelter(
            @PathVariable Long id,
            @Valid @RequestBody ShelterRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Shelter updated successfully",
                shelterService.updateShelter(id, request)
        );
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseDto<String> deleteShelter(
            @PathVariable Long id) {

        shelterService.deleteShelter(id);

        return new ApiResponseDto<>(
                true,
                "Shelter deleted successfully",
                null
        );
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<ShelterResponseDto> uploadShelterImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        return new ApiResponseDto<>(
                true,
                "Shelter image uploaded successfully",
                shelterService.uploadShelterImage(id, file)
        );
    }
}