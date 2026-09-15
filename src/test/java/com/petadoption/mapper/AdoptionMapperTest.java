package com.petadoption.mapper;

import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.ApplicationStatus;

import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class AdoptionMapperTest {

    private final AdoptionMapper mapper =
            Mappers.getMapper(
                    AdoptionMapper.class
            );

    @Test
    void shouldMapEntityToResponseDto() {

        Pet pet = new Pet();
        pet.setId(10L);
        pet.setName("Buddy");

        User adopter = new User();
        adopter.setId(20L);
        adopter.setEmail("adopter@test.com");

        AdoptionApplication application =
                new AdoptionApplication();

        application.setId(1L);
        application.setPet(pet);
        application.setAdopter(adopter);

        application.setApplicantNotes(
                "I have experience with dogs"
        );

        application.setStatus(
                ApplicationStatus.PENDING
        );

        AdoptionResponseDto dto =
                mapper.toResponseDto(
                        application
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
                "Buddy",
                dto.petName()
        );

        assertEquals(
                20L,
                dto.adopterId()
        );

        assertEquals(
                "adopter@test.com",
                dto.adopterEmail()
        );

        assertEquals(
                "I have experience with dogs",
                dto.applicantNotes()
        );

        assertEquals(
                ApplicationStatus.PENDING,
                dto.status()
        );
    }
}