package com.petadoption.service;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.ShelterResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ShelterService {

    ShelterResponseDto createShelter(
            ShelterRequestDto request);

    PageResponseDto<ShelterResponseDto> getAllShelters(
            Pageable pageable, String name);

    ShelterResponseDto getShelterById(Long id);

    ShelterResponseDto updateShelter(Long id, ShelterRequestDto request);

    void deleteShelter(Long id);

    ShelterResponseDto uploadShelterImage(Long id, MultipartFile file);
}
