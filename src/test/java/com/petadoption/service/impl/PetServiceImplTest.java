package com.petadoption.service.impl;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.PetMapper;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.PetImageRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ImageStorageService;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
class PetServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetMapper petMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AdoptionApplicationRepository adoptionApplicationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetImageRepository petImageRepository;

    @InjectMocks
    private PetServiceImpl petService;

    private Pet pet;
    private PetRequestDto requestDto;
    private PetResponseDto responseDto;
    private User shelterAdmin;

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
                        "Friendly dog",
                        EnergyLevel.MEDIUM,
                        Temperament.PLAYFUL,
                        false,
                        null
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
                        PetStatus.AVAILABLE,
                        null,
                        EnergyLevel.MEDIUM,
                        Temperament.PLAYFUL,
                        false,
                        null,
                        List.of(),
                        10L,
                        "Happy Paws"
                );

        Shelter shelter = new Shelter();
        shelter.setId(10L);
        shelter.setName("Happy Paws");

        shelterAdmin = new User();
        shelterAdmin.setId(2L);
        shelterAdmin.setEmail("admin@shelter.com");
        shelterAdmin.setShelter(shelter);
    }

    @Test
    void shouldCreatePet() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@shelter.com");

            when(userRepository.findByEmail("admin@shelter.com"))
                    .thenReturn(Optional.of(shelterAdmin));

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

            assertEquals(
                    shelterAdmin.getShelter(),
                    pet.getShelter()
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
    }

    @Test
    void shouldThrowWhenCreatingPetWithoutShelterAssignment() {

        shelterAdmin.setShelter(null);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@shelter.com");

            when(userRepository.findByEmail("admin@shelter.com"))
                    .thenReturn(Optional.of(shelterAdmin));

            assertThrows(
                    BusinessException.class,
                    () -> petService.createPet(requestDto)
            );

            verify(petRepository, never())
                    .save(any(Pet.class));
        }
    }

    @Test
    void shouldGetAllPets() {

        Pageable pageable = PageRequest.of(0, 20);

        when(petRepository.findAll(
                        any(org.springframework.data.jpa.domain.Specification.class),
                        eq(pageable)))
                .thenReturn(
                        new PageImpl<>(List.of(pet), pageable, 1)
                );

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        PageResponseDto<PetResponseDto> result =
                petService.getAllPets(pageable, null);

        assertEquals(
                1,
                result.content().size()
        );

        assertEquals(
                "Buddy",
                result.content().get(0).name()
        );

        assertEquals(
                1,
                result.totalElements()
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

        when(adoptionApplicationRepository.existsByPetId(1L))
                .thenReturn(false);

        when(appointmentRepository.existsByPetId(1L))
                .thenReturn(false);

        petService.deletePet(1L);

        verify(petRepository)
                .delete(pet);

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
    void shouldDeleteCloudinaryImageWhenDeletingPetWithImage() {

        pet.setImagePublicId("pet-adoption/pets/abc123");

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.of(pet)
                );

        when(adoptionApplicationRepository.existsByPetId(1L))
                .thenReturn(false);

        when(appointmentRepository.existsByPetId(1L))
                .thenReturn(false);

        petService.deletePet(1L);

        verify(imageStorageService)
                .deleteImage("pet-adoption/pets/abc123");

        verify(petRepository)
                .delete(pet);
    }

    @Test
    void shouldUploadPetImage() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "buddy.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(imageStorageService.uploadImage(file, "pet-adoption/pets"))
                .thenReturn(new ImageStorageService.ImageUploadResult(
                        "https://cdn.example.com/buddy.jpg",
                        "pet-adoption/pets/xyz789"));

        when(petRepository.save(any(Pet.class)))
                .thenReturn(pet);

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        PetResponseDto result =
                petService.uploadPetImage(1L, file);

        assertNotNull(result);

        assertEquals(
                "https://cdn.example.com/buddy.jpg",
                pet.getImageUrl());

        assertEquals(
                "pet-adoption/pets/xyz789",
                pet.getImagePublicId());

        verify(imageStorageService, never())
                .deleteImage(anyString());
    }

    @Test
    void shouldDeleteOldImageWhenReplacingPetImage() {

        pet.setImageUrl("https://cdn.example.com/old.jpg");
        pet.setImagePublicId("pet-adoption/pets/old123");

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "new.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(imageStorageService.uploadImage(file, "pet-adoption/pets"))
                .thenReturn(new ImageStorageService.ImageUploadResult(
                        "https://cdn.example.com/new.jpg",
                        "pet-adoption/pets/new456"));

        when(petRepository.save(any(Pet.class)))
                .thenReturn(pet);

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        petService.uploadPetImage(1L, file);

        verify(imageStorageService)
                .deleteImage("pet-adoption/pets/old123");
    }

    @Test
    void shouldThrowWhenUploadingEmptyImage() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.jpg",
                        "image/jpeg",
                        new byte[0]);

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        assertThrows(
                BusinessException.class,
                () -> petService.uploadPetImage(1L, file));

        verify(imageStorageService, never())
                .uploadImage(any(), anyString());
    }

    @Test
    void shouldThrowWhenUploadingNonImageFile() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "doc.pdf",
                        "application/pdf",
                        "not-an-image".getBytes());

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        assertThrows(
                BusinessException.class,
                () -> petService.uploadPetImage(1L, file));

        verify(imageStorageService, never())
                .uploadImage(any(), anyString());
    }

    @Test
    void shouldThrowWhenUploadingImageForMissingPet() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "buddy.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(petRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> petService.uploadPetImage(1L, file));
    }

    @Test
    void shouldThrowWhenDeletingPetWithExistingApplications() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.of(pet)
                );

        when(adoptionApplicationRepository.existsByPetId(1L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> petService.deletePet(1L)
        );

        verify(petRepository, never())
                .delete(any(Pet.class));
    }

    @Test
    void shouldThrowWhenDeletingPetWithExistingAppointments() {

        when(petRepository.findById(1L))
                .thenReturn(
                        Optional.of(pet)
                );

        when(adoptionApplicationRepository.existsByPetId(1L))
                .thenReturn(false);

        when(appointmentRepository.existsByPetId(1L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> petService.deletePet(1L)
        );

        verify(petRepository, never())
                .delete(any(Pet.class));
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

    @Test
    void shouldAddPetGalleryImage() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "gallery.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(imageStorageService.uploadImage(file, "pet-adoption/pets"))
                .thenReturn(new ImageStorageService.ImageUploadResult(
                        "https://cdn.example.com/gallery.jpg",
                        "pet-adoption/pets/gallery123"));

        when(petMapper.toResponseDto(pet))
                .thenReturn(responseDto);

        PetResponseDto result =
                petService.addPetGalleryImage(1L, file);

        assertNotNull(result);

        verify(petImageRepository).save(any());
    }

    @Test
    void shouldThrowWhenAddingGalleryImageForMissingPet() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "gallery.jpg",
                        "image/jpeg",
                        "fake-image-bytes".getBytes());

        when(petRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> petService.addPetGalleryImage(1L, file));
    }

    @Test
    void shouldThrowWhenAddingEmptyGalleryImage() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.jpg",
                        "image/jpeg",
                        new byte[0]);

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        assertThrows(
                BusinessException.class,
                () -> petService.addPetGalleryImage(1L, file));

        verify(petImageRepository, never()).save(any());
    }
}