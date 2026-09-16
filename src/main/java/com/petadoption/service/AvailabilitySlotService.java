package com.petadoption.service;

import com.petadoption.dto.request.AvailabilitySlotRequestDto;
import com.petadoption.dto.response.AvailabilitySlotResponseDto;

import java.util.List;

public interface AvailabilitySlotService {

    AvailabilitySlotResponseDto createSlot(
            AvailabilitySlotRequestDto request);

    List<AvailabilitySlotResponseDto> getOpenSlotsForShelter(
            Long shelterId);

    List<AvailabilitySlotResponseDto> getMySlots();

    void deleteSlot(Long id);
}
