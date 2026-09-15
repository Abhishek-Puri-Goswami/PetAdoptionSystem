package com.petadoption.service.impl;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.entity.Shelter;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.ShelterMapper;
import com.petadoption.repository.ShelterRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ShelterService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelterServiceImpl
        implements ShelterService {

    private final ShelterRepository shelterRepository;
    private final ShelterMapper shelterMapper;
    private final AuditLogService auditLogService;

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
    public List<ShelterResponseDto> getAllShelters() {

        return shelterRepository.findAll()
                .stream()
                .map(shelterMapper::toResponseDto)
                .toList();
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

        auditLogService.saveAuditLog(
                "SHELTER_DELETED",
                "Shelter",
                String.valueOf(shelter.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Shelter deleted");

        shelterRepository.delete(shelter);
    }
}