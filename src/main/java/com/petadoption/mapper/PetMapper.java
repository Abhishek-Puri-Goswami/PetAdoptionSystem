package com.petadoption.mapper;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    Pet toEntity(PetRequestDto dto);

    @Mapping(target = "shelterId", source = "shelter.id")
    @Mapping(target = "shelterName", source = "shelter.name")
    PetResponseDto toResponseDto(Pet pet);

}