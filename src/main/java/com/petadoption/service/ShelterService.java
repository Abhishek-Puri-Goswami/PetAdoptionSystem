package com.petadoption.service;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ShelterResponseDto;

import java.util.List;

public interface ShelterService {

    ShelterResponseDto createShelter(
            ShelterRequestDto request);

    List<ShelterResponseDto> getAllShelters();

    ShelterResponseDto getShelterById(Long id);

    ShelterResponseDto updateShelter(Long id, ShelterRequestDto request);

    void deleteShelter(Long id);
}