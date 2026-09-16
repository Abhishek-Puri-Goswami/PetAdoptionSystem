package com.petadoption.service;

import com.petadoption.dto.request.AdoptionDecisionRequestDto;
import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.dto.response.PageResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdoptionService {

    AdoptionResponseDto createApplication(
            AdoptionRequestDto request);

    PageResponseDto<AdoptionResponseDto> getAllApplications(
            Pageable pageable);

    List<AdoptionResponseDto> getMyApplications();

    AdoptionResponseDto getMyApplicationById(Long id);

    AdoptionResponseDto getApplicationById(Long id);

    AdoptionResponseDto approveApplication(
            Long id, AdoptionDecisionRequestDto decision);

    AdoptionResponseDto rejectApplication(
            Long id, AdoptionDecisionRequestDto decision);

}