package com.petadoption.service.impl;

import com.petadoption.dto.request.ShelterRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.ShelterResponseDto;
import com.petadoption.entity.Shelter;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.ShelterMapper;
import com.petadoption.repository.ShelterRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ImageStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private ImageStorageService imageStorageService;

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
                        "Animal Shelter",
                        null
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

        Pageable pageable = PageRequest.of(0, 20);

        when(shelterRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(shelter), pageable, 1));

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        PageResponseDto<ShelterResponseDto> result =
                shelterService.getAllShelters(pageable, null);

        assertEquals(
                1,
                result.content().size()
        );
    }

    @Test
    void shouldSearchSheltersByName() {

        Pageable pageable = PageRequest.of(0, 20);

        when(shelterRepository.findByNameContainingIgnoreCase(
                        "Happy", pageable))
                .thenReturn(new PageImpl<>(List.of(shelter), pageable, 1));

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        PageResponseDto<ShelterResponseDto> result =
                shelterService.getAllShelters(pageable, "Happy");

        assertEquals(
                1,
                result.content().size()
        );

        verify(shelterRepository, never()).findAll(pageable);
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

        verify(imageStorageService, never())
                .deleteImage(anyString());

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
    void shouldDeleteCloudinaryImageWhenDeletingShelterWithImage() {

        shelter.setImagePublicId("pet-adoption/shelters/abc123");

        when(shelterRepository.findById(1L))
                .thenReturn(
                        Optional.of(shelter)
                );

        shelterService.deleteShelter(1L);

        verify(imageStorageService)
                .deleteImage("pet-adoption/shelters/abc123");

        verify(shelterRepository)
                .delete(shelter);
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

    @Test
    void shouldUploadShelterImage() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "shelter.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(shelterRepository.findById(1L))
                .thenReturn(Optional.of(shelter));

        when(imageStorageService.uploadImage(
                file, "pet-adoption/shelters"))
                .thenReturn(new ImageStorageService.ImageUploadResult(
                        "https://cdn.example.com/shelter.jpg",
                        "pet-adoption/shelters/xyz789"));

        when(shelterRepository.save(any(Shelter.class)))
                .thenReturn(shelter);

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        ShelterResponseDto result =
                shelterService.uploadShelterImage(1L, file);

        assertNotNull(result);

        assertEquals(
                "https://cdn.example.com/shelter.jpg",
                shelter.getImageUrl());

        verify(imageStorageService, never())
                .deleteImage(anyString());
    }

    @Test
    void shouldDeleteOldImageWhenReplacingShelterImage() {

        shelter.setImageUrl("https://cdn.example.com/old.jpg");
        shelter.setImagePublicId("pet-adoption/shelters/old123");

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "new.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(shelterRepository.findById(1L))
                .thenReturn(Optional.of(shelter));

        when(imageStorageService.uploadImage(
                file, "pet-adoption/shelters"))
                .thenReturn(new ImageStorageService.ImageUploadResult(
                        "https://cdn.example.com/new.jpg",
                        "pet-adoption/shelters/new456"));

        when(shelterRepository.save(any(Shelter.class)))
                .thenReturn(shelter);

        when(shelterMapper.toResponseDto(shelter))
                .thenReturn(responseDto);

        shelterService.uploadShelterImage(1L, file);

        verify(imageStorageService)
                .deleteImage("pet-adoption/shelters/old123");
    }

    @Test
    void shouldThrowWhenUploadingEmptyShelterImage() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.jpg",
                        "image/jpeg",
                        new byte[0]);

        when(shelterRepository.findById(1L))
                .thenReturn(Optional.of(shelter));

        assertThrows(
                BusinessException.class,
                () -> shelterService.uploadShelterImage(1L, file));
    }

    @Test
    void shouldThrowWhenUploadingNonImageShelterFile() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "doc.pdf",
                        "application/pdf",
                        "not-an-image".getBytes());

        when(shelterRepository.findById(1L))
                .thenReturn(Optional.of(shelter));

        assertThrows(
                BusinessException.class,
                () -> shelterService.uploadShelterImage(1L, file));
    }

    @Test
    void shouldThrowWhenUploadingImageForMissingShelter() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "shelter.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(shelterRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> shelterService.uploadShelterImage(1L, file));
    }
}