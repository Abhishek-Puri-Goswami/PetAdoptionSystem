package com.petadoption.mapper;

import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.AppointmentResponseDto;
import com.petadoption.entity.Appointment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    Appointment toEntity(AppointmentRequestDto requestDto);

    @Mapping(target = "petId", source = "pet.id")
    @Mapping(target = "adopterId", source = "adopter.id")
    @Mapping(target = "shelterId", source = "shelter.id")
    AppointmentResponseDto toResponseDto(Appointment appointment);

    void updateEntityFromDto(
            AppointmentRequestDto requestDto,
            @MappingTarget Appointment appointment);
}