package com.petadoption.service.impl;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.PetMapper;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ImageStorageService;
import com.petadoption.service.PetService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private static final String PET_IMAGE_FOLDER = "pet-adoption/pets";

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final AuditLogService auditLogService;
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ImageStorageService imageStorageService;

    @Override
    public PetResponseDto createPet(PetRequestDto request) {

        Pet pet = petMapper.toEntity(request);

        Pet savedPet = petRepository.save(pet);

        auditLogService.saveAuditLog(
                "PET_CREATED",
                "Pet",
                String.valueOf(savedPet.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Pet created successfully");

        return petMapper.toResponseDto(savedPet);
    }

    @Override
    public List<PetResponseDto> getAllPets() {

        return petRepository.findAll()
                .stream()
                .map(petMapper::toResponseDto)
                .toList();
    }

    @Override
    public PetResponseDto getPetById(Long id) {

        Pet pet =
                petRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        return petMapper.toResponseDto(pet);
    }

    @Override
    public PetResponseDto updatePet(
            Long id,
            PetRequestDto request) {

        Pet pet =
                petRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        pet.setName(request.name());
        pet.setSpecies(request.species());
        pet.setBreed(request.breed());
        pet.setAge(request.age());
        pet.setGender(request.gender());
        pet.setDescription(request.description());

        Pet updatedPet =
                petRepository.save(pet);

        auditLogService.saveAuditLog(
                "PET_UPDATED",
                "Pet",
                String.valueOf(updatedPet.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Pet updated successfully");

        return petMapper.toResponseDto(updatedPet);
    }

    @Override
    public void deletePet(Long id) {

        Pet pet =
                petRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        if (adoptionApplicationRepository.existsByPetId(id)
                || appointmentRepository.existsByPetId(id)) {

            throw new BusinessException(
                    "Cannot delete a pet with existing adoption "
                            + "applications or appointments; "
                            + "mark it UNAVAILABLE instead");
        }

        if (StringUtils.hasText(pet.getImagePublicId())) {
            imageStorageService.deleteImage(pet.getImagePublicId());
        }

        auditLogService.saveAuditLog(
                "PET_DELETED",
                "Pet",
                String.valueOf(pet.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Pet deleted successfully");

        petRepository.delete(pet);
    }

    @Override
    public PetResponseDto uploadPetImage(Long id, MultipartFile file) {

        Pet pet =
                petRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Image file is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(
                    "Only image files are allowed");
        }

        String oldPublicId = pet.getImagePublicId();

        ImageStorageService.ImageUploadResult result =
                imageStorageService.uploadImage(file, PET_IMAGE_FOLDER);

        pet.setImageUrl(result.url());
        pet.setImagePublicId(result.publicId());

        Pet updatedPet = petRepository.save(pet);

        if (StringUtils.hasText(oldPublicId)) {
            imageStorageService.deleteImage(oldPublicId);
        }

        auditLogService.saveAuditLog(
                "PET_IMAGE_UPDATED",
                "Pet",
                String.valueOf(updatedPet.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Pet image uploaded/replaced");

        return petMapper.toResponseDto(updatedPet);
    }
}