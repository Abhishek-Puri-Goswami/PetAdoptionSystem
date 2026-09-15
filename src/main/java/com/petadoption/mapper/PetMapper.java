package com.petadoption.mapper;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PetMapper {

    Pet toEntity(PetRequestDto dto);

    PetResponseDto toResponseDto(Pet pet);

}