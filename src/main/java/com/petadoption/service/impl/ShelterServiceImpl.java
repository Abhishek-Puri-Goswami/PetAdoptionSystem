package com.petadoption.service.impl;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.entity.Shelter;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.ShelterMapper;
import com.petadoption.repository.ShelterRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ImageStorageService;
import com.petadoption.service.ShelterService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShelterServiceImpl
        implements ShelterService {

    private static final String SHELTER_IMAGE_FOLDER =
            "pet-adoption/shelters";

    private final ShelterRepository shelterRepository;
    private final ShelterMapper shelterMapper;
    private final AuditLogService auditLogService;
    private final ImageStorageService imageStorageService;

    @Override
    public ShelterResponseDto createShelter(
            ShelterRequestDto request) {

        Shelter shelter = shelterMapper.toEntity(request);

        Shelter saved = shelterRepository.save(shelter);

        auditLogService.saveAuditLog(
                "SHELTER_CREATED",
                "Shelter",
                String.valueOf(saved.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Shelter created");

        return shelterMapper.toResponseDto(saved);
    }

    @Override
    public PageResponseDto<ShelterResponseDto> getAllShelters(
            Pageable pageable, String name) {

        Page<Shelter> shelters =
                StringUtils.hasText(name)
                        ? shelterRepository
                                .findByNameContainingIgnoreCase(
                                        name, pageable)
                        : shelterRepository.findAll(pageable);

        return PageResponseDto.from(
                shelters.map(shelterMapper::toResponseDto));
    }

    @Override
    public ShelterResponseDto getShelterById(Long id) {

        Shelter shelter =
                shelterRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Shelter not found"));

        return shelterMapper.toResponseDto(shelter);
    }

    @Override
    public ShelterResponseDto updateShelter(
            Long id,
            ShelterRequestDto request) {

        Shelter shelter =
                shelterRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Shelter not found"));

        shelter.setName(request.name());
        shelter.setEmail(request.email());
        shelter.setPhone(request.phone());
        shelter.setAddressLine1(request.addressLine1());
        shelter.setAddressLine2(request.addressLine2());
        shelter.setCity(request.city());
        shelter.setState(request.state());
        shelter.setPostalCode(request.postalCode());
        shelter.setCountry(request.country());
        shelter.setDescription(request.description());

        Shelter updated = shelterRepository.save(shelter);

        auditLogService.saveAuditLog(
                "SHELTER_UPDATED",
                "Shelter",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Shelter updated");

        return shelterMapper.toResponseDto(updated);
    }

    @Override
    public void deleteShelter(Long id) {

        Shelter shelter =
                shelterRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Shelter not found"));

        if (StringUtils.hasText(shelter.getImagePublicId())) {
            imageStorageService.deleteImage(shelter.getImagePublicId());
        }

        auditLogService.saveAuditLog(
                "SHELTER_DELETED",
                "Shelter",
                String.valueOf(shelter.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Shelter deleted");

        shelterRepository.delete(shelter);
    }

    @Override
    public ShelterResponseDto uploadShelterImage(
            Long id, MultipartFile file) {

        Shelter shelter =
                shelterRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Shelter not found"));

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Image file is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(
                    "Only image files are allowed");
        }

        String oldPublicId = shelter.getImagePublicId();

        ImageStorageService.ImageUploadResult result =
                imageStorageService.uploadImage(
                        file, SHELTER_IMAGE_FOLDER);

        shelter.setImageUrl(result.url());
        shelter.setImagePublicId(result.publicId());

        Shelter updated = shelterRepository.save(shelter);

        if (StringUtils.hasText(oldPublicId)) {
            imageStorageService.deleteImage(oldPublicId);
        }

        auditLogService.saveAuditLog(
                "SHELTER_IMAGE_UPDATED",
                "Shelter",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Shelter image uploaded/replaced");

        return shelterMapper.toResponseDto(updated);
    }
}