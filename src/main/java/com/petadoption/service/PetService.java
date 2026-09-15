package com.petadoption.service;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;

import java.util.List;

public interface PetService {

    PetResponseDto createPet(PetRequestDto request);

    List<PetResponseDto> getAllPets();

    PetResponseDto getPetById(Long id);

    PetResponseDto updatePet(Long id, PetRequestDto request);

    void deletePet(Long id);

}