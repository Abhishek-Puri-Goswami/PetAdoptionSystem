package com.petadoption.service.impl;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.request.PetSearchCriteria;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.entity.PetImage;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.PetMapper;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.PetImageRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.PetSpecification;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ImageStorageService;
import com.petadoption.service.PetService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private final UserRepository userRepository;
    private final PetImageRepository petImageRepository;

    @Override
    public PetResponseDto createPet(PetRequestDto request) {

        String email = SecurityUtil.getCurrentUserEmail();

        User creator =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        if (creator.getShelter() == null) {
            throw new BusinessException(
                    "You must be assigned to a shelter before "
                            + "creating pets");
        }

        Pet pet = petMapper.toEntity(request);
        pet.setShelter(creator.getShelter());

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
    public PageResponseDto<PetResponseDto> getAllPets(
            Pageable pageable, PetSearchCriteria criteria) {

        Page<PetResponseDto> page =
                petRepository.findAll(
                                PetSpecification.fromCriteria(criteria),
                                pageable)
                        .map(petMapper::toResponseDto);

        return PageResponseDto.from(page);
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
        pet.setEnergyLevel(request.energyLevel());
        pet.setTemperament(request.temperament());
        pet.setSterilized(request.sterilized());
        pet.setSpecialCareNotes(request.specialCareNotes());

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

    @Override
    public PetResponseDto addPetGalleryImage(
            Long id, MultipartFile file) {

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

        ImageStorageService.ImageUploadResult result =
                imageStorageService.uploadImage(file, PET_IMAGE_FOLDER);

        PetImage image = new PetImage();
        image.setPet(pet);
        image.setImageUrl(result.url());
        image.setImagePublicId(result.publicId());

        petImageRepository.save(image);

        auditLogService.saveAuditLog(
                "PET_GALLERY_IMAGE_ADDED",
                "Pet",
                String.valueOf(pet.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Pet gallery image added");

        Pet refreshed =
                petRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        return petMapper.toResponseDto(refreshed);
    }
}