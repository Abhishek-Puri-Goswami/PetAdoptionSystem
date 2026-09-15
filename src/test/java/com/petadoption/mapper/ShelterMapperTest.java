package com.petadoption.mapper;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.entity.Shelter;

import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ShelterMapperTest {

    private final ShelterMapper mapper =
            Mappers.getMapper(
                    ShelterMapper.class
            );

    @Test
    void shouldMapRequestDtoToEntity() {

        ShelterRequestDto dto =
                new ShelterRequestDto(
                        "Happy Paws",
                        "shelter@test.com",
                        "9876543210",
                        "Address Line 1",
                        "Address Line 2",
                        "Bangalore",
                        "Karnataka",
                        "560001",
                        "India",
                        "Animal shelter"
                );

        Shelter shelter =
                mapper.toEntity(dto);

        assertNotNull(shelter);

        assertEquals(
                "Happy Paws",
                shelter.getName()
        );

        assertEquals(
                "shelter@test.com",
                shelter.getEmail()
        );

        assertEquals(
                "9876543210",
                shelter.getPhone()
        );

        assertEquals(
                "Bangalore",
                shelter.getCity()
        );

        assertEquals(
                "Karnataka",
                shelter.getState()
        );

        assertEquals(
                "India",
                shelter.getCountry()
        );

        assertEquals(
                "Animal shelter",
                shelter.getDescription()
        );
    }

    @Test
    void shouldMapEntityToResponseDto() {

        Shelter shelter =
                new Shelter();

        shelter.setId(1L);
        shelter.setName("Happy Paws");
        shelter.setEmail("shelter@test.com");
        shelter.setPhone("9876543210");
        shelter.setCity("Bangalore");
        shelter.setState("Karnataka");
        shelter.setCountry("India");
        shelter.setDescription("Animal shelter");
        shelter.setImageUrl("https://cdn.example.com/shelter.jpg");

        ShelterResponseDto dto =
                mapper.toResponseDto(
                        shelter
                );

        assertNotNull(dto);

        assertEquals(
                1L,
                dto.id()
        );

        assertEquals(
                "Happy Paws",
                dto.name()
        );

        assertEquals(
                "shelter@test.com",
                dto.email()
        );

        assertEquals(
                "9876543210",
                dto.phone()
        );

        assertEquals(
                "Bangalore",
                dto.city()
        );

        assertEquals(
                "Karnataka",
                dto.state()
        );

        assertEquals(
                "India",
                dto.country()
        );

        assertEquals(
                "Animal shelter",
                dto.description()
        );

        assertEquals(
                "https://cdn.example.com/shelter.jpg",
                dto.imageUrl()
        );
    }
}