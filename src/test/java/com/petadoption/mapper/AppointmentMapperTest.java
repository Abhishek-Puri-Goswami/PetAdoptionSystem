package com.petadoption.mapper;

import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.AppointmentResponseDto;
import com.petadoption.entity.Appointment;
import com.petadoption.entity.AvailabilitySlot;
import com.petadoption.entity.Pet;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.enums.AppointmentStatus;

import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentMapperTest {

    private final AppointmentMapper mapper =
            Mappers.getMapper(
                    AppointmentMapper.class
            );

    @Test
    void shouldMapRequestDtoToEntity() {

        AppointmentRequestDto dto =
                new AppointmentRequestDto(
                        1L,
                        2L,
                        "Visit pet"
                );

        Appointment appointment =
                mapper.toEntity(dto);

        assertNotNull(appointment);

        assertEquals(
                "Visit pet",
                appointment.getNotes()
        );
    }

    @Test
    void shouldMapEntityToResponseDto() {

        Pet pet = new Pet();
        pet.setId(10L);

        User adopter = new User();
        adopter.setId(20L);

        Shelter shelter = new Shelter();
        shelter.setId(30L);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(40L);

        Appointment appointment =
                new Appointment();

        appointment.setId(1L);
        appointment.setPet(pet);
        appointment.setAdopter(adopter);
        appointment.setShelter(shelter);
        appointment.setSlot(slot);

        appointment.setAppointmentDateTime(
                LocalDateTime.of(
                        2030,
                        1,
                        1,
                        10,
                        0
                )
        );

        appointment.setNotes(
                "Meet and greet"
        );

        appointment.setStatus(
                AppointmentStatus.APPROVED
        );

        AppointmentResponseDto dto =
                mapper.toResponseDto(
                        appointment
                );

        assertNotNull(dto);

        assertEquals(
                1L,
                dto.id()
        );

        assertEquals(
                10L,
                dto.petId()
        );

        assertEquals(
                20L,
                dto.adopterId()
        );

        assertEquals(
                30L,
                dto.shelterId()
        );

        assertEquals(
                40L,
                dto.slotId()
        );

        assertEquals(
                "Meet and greet",
                dto.notes()
        );
    }

    @Test
    void shouldUpdateEntityFromDto() {

        Appointment appointment =
                new Appointment();

        AppointmentRequestDto dto =
                new AppointmentRequestDto(
                        1L,
                        2L,
                        "Updated notes"
                );

        mapper.updateEntityFromDto(
                dto,
                appointment
        );

        assertEquals(
                "Updated notes",
                appointment.getNotes()
        );
    }
}