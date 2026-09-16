package com.petadoption.controller;

import com.petadoption.dto.request.AvailabilitySlotRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.AvailabilitySlotResponseDto;
import com.petadoption.service.AvailabilitySlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class AvailabilitySlotController {

    private final AvailabilitySlotService slotService;

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @PostMapping
    public ApiResponseDto<AvailabilitySlotResponseDto> createSlot(
            @Valid @RequestBody AvailabilitySlotRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Slot created successfully",
                slotService.createSlot(request)
        );
    }

    @PreAuthorize(
            "hasAnyRole('ADOPTER','SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping("/shelter/{shelterId}")
    public ApiResponseDto<List<AvailabilitySlotResponseDto>>
    getOpenSlotsForShelter(@PathVariable Long shelterId) {

        return new ApiResponseDto<>(
                true,
                "Open slots fetched successfully",
                slotService.getOpenSlotsForShelter(shelterId)
        );
    }

    @PreAuthorize(
            "hasAnyRole('SHELTER_ADMIN','SHELTER_STAFF','SYSTEM_ADMIN')")
    @GetMapping("/my")
    public ApiResponseDto<List<AvailabilitySlotResponseDto>> getMySlots() {

        return new ApiResponseDto<>(
                true,
                "Slots fetched successfully",
                slotService.getMySlots()
        );
    }

    @PreAuthorize("hasRole('SHELTER_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseDto<String> deleteSlot(@PathVariable Long id) {

        slotService.deleteSlot(id);

        return new ApiResponseDto<>(
                true,
                "Slot deleted successfully",
                null
        );
    }
}
