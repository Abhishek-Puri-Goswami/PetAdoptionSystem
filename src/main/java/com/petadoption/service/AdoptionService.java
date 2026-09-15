package com.petadoption.service;

import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;

import java.util.List;

public interface AdoptionService {

    AdoptionResponseDto createApplication(
            AdoptionRequestDto request);

    List<AdoptionResponseDto> getAllApplications();

    List<AdoptionResponseDto> getMyApplications();

    AdoptionResponseDto getMyApplicationById(Long id);

    AdoptionResponseDto getApplicationById(Long id);

    AdoptionResponseDto approveApplication(Long id);

    AdoptionResponseDto rejectApplication(Long id);

}