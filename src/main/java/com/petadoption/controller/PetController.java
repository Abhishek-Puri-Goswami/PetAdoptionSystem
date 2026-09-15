package com.petadoption.controller;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.service.PetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResponseDto<List<PetResponseDto>> getAllPets() {

        return new ApiResponseDto<>(
                true,
                "Pets fetched successfully",
                petService.getAllPets()
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
}