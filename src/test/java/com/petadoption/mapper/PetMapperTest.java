package com.petadoption.mapper;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;

import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class PetMapperTest {

    private final PetMapper mapper =
            Mappers.getMapper(
                    PetMapper.class
            );

    @Test
    void shouldMapRequestDtoToEntity() {

        PetRequestDto dto =
                new PetRequestDto(
                        "Buddy",
                        "Dog",
                        "Labrador",
                        2,
                        "Male",
                        "Friendly dog",
                        EnergyLevel.MEDIUM,
                        Temperament.PLAYFUL
                );

        Pet pet =
                mapper.toEntity(dto);

        assertNotNull(pet);

        assertEquals(
                "Buddy",
                pet.getName()
        );

        assertEquals(
                "Dog",
                pet.getSpecies()
        );

        assertEquals(
                "Labrador",
                pet.getBreed()
        );

        assertEquals(
                2,
                pet.getAge()
        );

        assertEquals(
                "Male",
                pet.getGender()
        );

        assertEquals(
                "Friendly dog",
                pet.getDescription()
        );

        assertEquals(
                EnergyLevel.MEDIUM,
                pet.getEnergyLevel()
        );

        assertEquals(
                Temperament.PLAYFUL,
                pet.getTemperament()
        );
    }

    @Test
    void shouldMapEntityToResponseDto() {

        Pet pet = new Pet();

        pet.setId(1L);
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setBreed("Labrador");
        pet.setAge(2);
        pet.setGender("Male");
        pet.setDescription("Friendly dog");
        pet.setStatus(PetStatus.AVAILABLE);
        pet.setImageUrl("https://cdn.example.com/buddy.jpg");
        pet.setEnergyLevel(EnergyLevel.HIGH);
        pet.setTemperament(Temperament.AFFECTIONATE);

        PetResponseDto dto =
                mapper.toResponseDto(pet);

        assertNotNull(dto);

        assertEquals(
                1L,
                dto.id()
        );

        assertEquals(
                "Buddy",
                dto.name()
        );

        assertEquals(
                "Dog",
                dto.species()
        );

        assertEquals(
                "Labrador",
                dto.breed()
        );

        assertEquals(
                2,
                dto.age()
        );

        assertEquals(
                "Male",
                dto.gender()
        );

        assertEquals(
                "Friendly dog",
                dto.description()
        );

        assertEquals(
                PetStatus.AVAILABLE,
                dto.status()
        );

        assertEquals(
                "https://cdn.example.com/buddy.jpg",
                dto.imageUrl()
        );

        assertEquals(
                EnergyLevel.HIGH,
                dto.energyLevel()
        );

        assertEquals(
                Temperament.AFFECTIONATE,
                dto.temperament()
        );
    }
}