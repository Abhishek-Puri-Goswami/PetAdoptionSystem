package com.petadoption.service;

import com.petadoption.dto.request.MedicalRecordRequestDto;
import com.petadoption.dto.response.MedicalRecordResponseDto;

import java.util.List;

public interface MedicalRecordService {

    MedicalRecordResponseDto createRecord(
            Long petId, MedicalRecordRequestDto request);

    List<MedicalRecordResponseDto> getRecordsForPet(Long petId);

}
