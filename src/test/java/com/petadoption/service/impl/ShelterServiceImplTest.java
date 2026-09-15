package com.petadoption.service.impl;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.entity.Shelter;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.ShelterMapper;
import com.petadoption.repository.ShelterRepository;
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
class ShelterServiceImplTest {

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private ShelterMapper shelterMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ShelterServiceImpl shelterService;

    private Shelter shelter;
    private ShelterRequestDto requestDto;
    private ShelterResponseDto responseDto;

    @BeforeEach
    void setUp() {

        shelter = new Shelter();

        shelter.setId(1L);
        shelter.setName("Happy Paws");
        shelter.setEmail("test@test.com");

        requestDto =
                new ShelterRequestDto(
                        "Happy Paws",
                        "test@test.com",
                        "9876543210",
                        "Address1",
                        "Address2",
                        "Bangalore",
                        "Karnataka",
                        "560001",
                        "India",
                        "Animal Shelter"
                );

        responseDto =
                new ShelterResponseDto(
                        1L,
                        "Happy Paws",
                        "test@test.com",
                        "9876543210",
                        "Bangalore",
                        "Karnataka",
                        "India",
                        "Animal Shelter"
                );
    }

    @Test
    void shouldCreateShelter() {

        when(shelterMapper.toEntity(requestDto))
                .thenReturn(shelter);

        when(shelterRepository.save(shelter))
                .thenReturn(shelter);

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        ShelterResponseDto result =
                shelterService.createShelter(
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
    void shouldGetAllShelters() {

        when(shelterRepository.findAll())
                .thenReturn(List.of(shelter));

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        List<ShelterResponseDto> result =
                shelterService.getAllShelters();

        assertEquals(
                1,
                result.size()
        );
    }

    @Test
    void shouldGetShelterById() {

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.of(shelter)
                );

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        ShelterResponseDto result =
                shelterService.getShelterById(
                        1L
                );

        assertEquals(
                1L,
                result.id()
        );
    }

    @Test
    void shouldThrowWhenShelterNotFound() {

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> shelterService.getShelterById(
                        1L
                )
        );
    }

    @Test
    void shouldUpdateShelter() {

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.of(shelter)
                );

        when(shelterRepository.save(any()))
                .thenReturn(shelter);

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        ShelterResponseDto result =
                shelterService.updateShelter(
                        1L,
                        requestDto
                );

        assertNotNull(result);

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
    void shouldThrowWhenUpdateShelterNotFound() {

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> shelterService.updateShelter(
                        1L,
                        requestDto
                )
        );
    }

    @Test
    void shouldDeleteShelter() {

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.of(shelter)
                );

        shelterService.deleteShelter(1L);

        verify(shelterRepository)
                .delete(shelter);

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
    void shouldThrowWhenDeleteShelterNotFound() {

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> shelterService.deleteShelter(
                        1L
                )
        );
    }
}