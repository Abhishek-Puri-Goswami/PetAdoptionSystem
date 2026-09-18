package com.petadoption.service;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.request.PetSearchCriteria;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.PetResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PetService {

    PetResponseDto createPet(PetRequestDto request);

    PageResponseDto<PetResponseDto> getAllPets(
            Pageable pageable, PetSearchCriteria criteria);

    PetResponseDto getPetById(Long id);

    PetResponseDto updatePet(Long id, PetRequestDto request);

    void deletePet(Long id);

    PetResponseDto uploadPetImage(Long id, MultipartFile file);

    PetResponseDto addPetGalleryImage(Long id, MultipartFile file);

    PetResponseDto replacePetGalleryImage(
            Long petId, Long imageId, MultipartFile file);

    PetResponseDto deletePetGalleryImage(Long petId, Long imageId);

}
