package com.petadoption.mapper;

import com.petadoption.dto.response.MedicalRecordResponseDto;
import com.petadoption.entity.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(target = "petId", source = "pet.id")
    MedicalRecordResponseDto toResponseDto(MedicalRecord record);

}
