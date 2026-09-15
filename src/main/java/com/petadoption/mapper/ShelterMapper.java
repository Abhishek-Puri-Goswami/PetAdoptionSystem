package com.petadoption.mapper;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.entity.Shelter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShelterMapper {

    Shelter toEntity(ShelterRequestDto dto);

    ShelterResponseDto toResponseDto(Shelter shelter);
}