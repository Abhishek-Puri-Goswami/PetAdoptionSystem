package com.petadoption.service.impl;

import com.petadoption.dto.request.MedicalRecordRequestDto;
import com.petadoption.dto.response.MedicalRecordResponseDto;
import com.petadoption.entity.MedicalRecord;
import com.petadoption.entity.Pet;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.MedicalRecordMapper;
import com.petadoption.repository.MedicalRecordRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.MedicalRecordService;
import com.petadoption.service.ShelterScopeService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PetRepository petRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final AuditLogService auditLogService;
    private final ShelterScopeService shelterScopeService;

    @Override
    public MedicalRecordResponseDto createRecord(
            Long petId, MedicalRecordRequestDto request) {

        Pet pet =
                petRepository.findById(petId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                shelterIdOf(pet));

        MedicalRecord record = new MedicalRecord();
        record.setPet(pet);
        record.setRecordDate(request.recordDate());
        record.setDescription(request.description());
        record.setVetName(request.vetName());

        MedicalRecord saved = medicalRecordRepository.save(record);

        auditLogService.saveAuditLog(
                "MEDICAL_RECORD_CREATED",
                "MedicalRecord",
                String.valueOf(saved.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Medical record created for pet " + petId);

        return medicalRecordMapper.toResponseDto(saved);
    }

    @Override
    public List<MedicalRecordResponseDto> getRecordsForPet(Long petId) {

        Pet pet =
                petRepository.findById(petId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                shelterIdOf(pet));

        return medicalRecordRepository.findByPetId(petId)
                .stream()
                .map(medicalRecordMapper::toResponseDto)
                .toList();
    }

    private Long shelterIdOf(Pet pet) {
        return pet != null && pet.getShelter() != null
                ? pet.getShelter().getId()
                : null;
    }
}
