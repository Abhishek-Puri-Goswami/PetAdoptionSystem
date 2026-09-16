package com.petadoption.mapper;

import com.petadoption.dto.response.AvailabilitySlotResponseDto;
import com.petadoption.entity.AvailabilitySlot;
import com.petadoption.entity.Shelter;

import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilitySlotMapperTest {

    private final AvailabilitySlotMapper mapper =
            Mappers.getMapper(
                    AvailabilitySlotMapper.class
            );

    @Test
    void shouldMapEntityToResponseDto() {

        Shelter shelter = new Shelter();
        shelter.setId(1L);
        shelter.setName("Happy Paws");

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(5L);
        slot.setShelter(shelter);
        slot.setSlotDateTime(
                LocalDateTime.of(2030, 1, 1, 10, 0));
        slot.setBooked(true);

        AvailabilitySlotResponseDto dto =
                mapper.toResponseDto(slot);

        assertNotNull(dto);

        assertEquals(5L, dto.id());

        assertEquals(
                LocalDateTime.of(2030, 1, 1, 10, 0),
                dto.slotDateTime()
        );

        assertTrue(dto.booked());

        assertEquals(1L, dto.shelterId());

        assertEquals("Happy Paws", dto.shelterName());
    }
}
