package com.petadoption.mapper;

import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.entity.AdoptionApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdoptionMapper {

    @Mapping(source = "pet.id", target = "petId")
    @Mapping(source = "pet.name", target = "petName")
    @Mapping(source = "adopter.id", target = "adopterId")
    @Mapping(source = "adopter.email", target = "adopterEmail")
    AdoptionResponseDto toResponseDto(
            AdoptionApplication adoptionApplication);

}