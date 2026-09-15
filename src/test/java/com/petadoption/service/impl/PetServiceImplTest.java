package com.petadoption.service.impl;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.enums.PetStatus;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.PetMapper;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.AuditLogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetMapper petMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PetServiceImpl petService;

    private Pet pet;
    private PetRequestDto requestDto;
    private PetResponseDto responseDto;

    @BeforeEach
    void setUp() {

        pet = new Pet();

        pet.setId(1L);
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setBreed("Labrador");
        pet.setAge(2);
        pet.setGender("Male");
        pet.setDescription("Friendly dog");
        pet.setStatus(PetStatus.AVAILABLE);

        requestDto =
                new PetRequestDto(
                        "Buddy",
                        "Dog",
                        "Labrador",
                        2,
                        "Male",
                        "Friendly dog"
                );

        responseDto =
                new PetResponseDto(
                        1L,
                        "Buddy",
                        "Dog",
                        "Labrador",
                        2,
                        "Male",
                        "Friendly dog",
                        PetStatus.AVAILABLE
                );
    }

    @Test
    void shouldCreatePet() {

        when(petMapper.toEntity(requestDto))
                .thenReturn(pet);

        when(petRepository.save(pet))
                .thenReturn(pet);

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        PetResponseDto result =
                petService.createPet(
                        requestDto
                );

        assertNotNull(result);

        assertEquals(
                1L,
                result.id()
        );

        verify(auditLogService)
                .saveAuditLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldGetAllPets() {

        when(petRepository.findAll())
                .thenReturn(
                        List.of(pet)
                );

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        List<PetResponseDto> result =
                petService.getAllPets();

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Buddy",
                result.get(0).name()
        );
    }

    @Test
    void shouldGetPetById() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.of(pet)
                );

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        PetResponseDto result =
                petService.getPetById(
                        1L
                );

        assertEquals(
                1L,
                result.id()
        );
    }

    @Test
    void shouldThrowWhenPetNotFoundById() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> petService.getPetById(
                        1L
                )
        );
    }

    @Test
    void shouldUpdatePet() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.of(pet)
                );

        when(petRepository.save(any(Pet.class)))
                .thenReturn(pet);

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        PetResponseDto result =
                petService.updatePet(
                        1L,
                        requestDto
                );

        assertNotNull(result);

        assertEquals(
                "Buddy",
                result.name()
        );

        verify(auditLogService)
                .saveAuditLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldThrowWhenUpdatingMissingPet() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> petService.updatePet(
                        1L,
                        requestDto
                )
        );
    }

    @Test
    void shouldDeletePet() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.of(pet)
                );

        petService.deletePet(1L);

        verify(petRepository)
                .delete(pet);

        verify(auditLogService)
                .saveAuditLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldThrowWhenDeletingMissingPet() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> petService.deletePet(
                        1L
                )
        );
    }
}