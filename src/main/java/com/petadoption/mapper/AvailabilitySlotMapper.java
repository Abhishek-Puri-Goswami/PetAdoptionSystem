package com.petadoption.mapper;

import com.petadoption.dto.response.AvailabilitySlotResponseDto;
import com.petadoption.entity.AvailabilitySlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvailabilitySlotMapper {

    @Mapping(target = "shelterId", source = "shelter.id")
    @Mapping(target = "shelterName", source = "shelter.name")
    AvailabilitySlotResponseDto toResponseDto(AvailabilitySlot slot);

}
