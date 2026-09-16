package com.petadoption.controller;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.request.PetSearchCriteria;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;
import com.petadoption.service.PetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PostMapping
    public ApiResponseDto<PetResponseDto> createPet(
            @Valid @RequestBody PetRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Pet created successfully",
                petService.createPet(request)
        );
    }

    @PreAuthorize(
            "hasAnyRole('ADOPTER','SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping
    public ApiResponseDto<PageResponseDto<PetResponseDto>> getAllPets(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) EnergyLevel energyLevel,
            @RequestParam(required = false) Temperament temperament,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) PetStatus status,
            @RequestParam(required = false) String search) {

        PetSearchCriteria criteria = new PetSearchCriteria(
                species, energyLevel, temperament,
                minAge, maxAge, status, search);

        return new ApiResponseDto<>(
                true,
                "Pets fetched successfully",
                petService.getAllPets(pageable, criteria)
        );
    }

    @PreAuthorize(
            "hasAnyRole('ADOPTER','SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping("/{id}")
    public ApiResponseDto<PetResponseDto> getPetById(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "Pet fetched successfully",
                petService.getPetById(id)
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseDto<PetResponseDto> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Pet updated successfully",
                petService.updatePet(id, request)
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseDto<String> deletePet(
            @PathVariable Long id) {

        petService.deletePet(id);

        return new ApiResponseDto<>(
                true,
                "Pet deleted successfully",
                null
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PostMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<PetResponseDto> uploadPetImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        return new ApiResponseDto<>(
                true,
                "Pet image uploaded successfully",
                petService.uploadPetImage(id, file)
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PostMapping(
            value = "/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<PetResponseDto> addPetGalleryImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        return new ApiResponseDto<>(
                true,
                "Pet gallery image added successfully",
                petService.addPetGalleryImage(id, file)
        );
    }
}