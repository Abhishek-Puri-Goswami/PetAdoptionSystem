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

    @Mock
    private com.petadoption.service.ShelterScopeService shelterScopeService;

    @Mock
    private com.petadoption.repository.MedicalRecordRepository
            medicalRecordRepository;

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

    private static final com.petadoption.dto.request.PetSearchCriteria
            EMPTY_CRITERIA = new com.petadoption.dto.request
            .PetSearchCriteria(null, null, null, null, null, null, null);


    private com.petadoption.entity.PetImage galleryImage(
            Long id, String publicId) {
        com.petadoption.entity.PetImage image =
                new com.petadoption.entity.PetImage();
        image.setId(id);
        image.setImageUrl("https://cdn/" + publicId);
        image.setImagePublicId(publicId);
        image.setPet(pet);
        return image;
    }

    private org.springframework.mock.web.MockMultipartFile imageFile() {
        return new org.springframework.mock.web.MockMultipartFile(
                "file", "a.png", "image/png", new byte[]{1, 2, 3});
    }

    @Test
    void shouldDeleteGalleryImageFromDbAndCloudinary() {

        com.petadoption.entity.PetImage image = galleryImage(7L, "pub/7");

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petImageRepository.findByIdAndPetId(7L, 1L))
                .thenReturn(Optional.of(image));
        when(petMapper.toResponseDto(pet)).thenReturn(responseDto);

        petService.deletePetGalleryImage(1L, 7L);

        verify(shelterScopeService).verifyShelterAccess(any(), any());
        verify(petImageRepository).delete(image);
        verify(imageStorageService).deleteImage("pub/7");
    }

    @Test
    void shouldNotFindGalleryImageUnderWrongPet() {

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petImageRepository.findByIdAndPetId(7L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> petService.deletePetGalleryImage(1L, 7L));

        verify(petImageRepository, never()).delete(any());
        verify(imageStorageService, never()).deleteImage(anyString());
    }

    @Test
    void shouldRefuseGalleryDeleteForOtherShelter() {

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        doThrow(new BusinessException("no access"))
                .when(shelterScopeService)
                .verifyShelterAccess(any(), any());

        assertThrows(BusinessException.class,
                () -> petService.deletePetGalleryImage(1L, 7L));

        verify(petImageRepository, never()).delete(any());
        verify(imageStorageService, never()).deleteImage(anyString());
    }

    @Test
    void shouldReplaceGalleryImageAndDeleteOldAsset() {

        com.petadoption.entity.PetImage image = galleryImage(7L, "pub/old");

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petImageRepository.findByIdAndPetId(7L, 1L))
                .thenReturn(Optional.of(image));
        when(imageStorageService.uploadImage(any(), anyString()))
                .thenReturn(new ImageStorageService.ImageUploadResult(
                        "https://cdn/new", "pub/new"));
        when(petMapper.toResponseDto(pet)).thenReturn(responseDto);

        petService.replacePetGalleryImage(1L, 7L, imageFile());

        assertEquals("pub/new", image.getImagePublicId());
        assertEquals("https://cdn/new", image.getImageUrl());
        verify(petImageRepository).save(image);
        verify(imageStorageService).deleteImage("pub/old");
    }

    @Test
    void shouldRejectNonImageOnGalleryReplace() {

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        org.springframework.mock.web.MockMultipartFile pdf =
                new org.springframework.mock.web.MockMultipartFile(
                        "file", "a.pdf", "application/pdf",
                        new byte[]{1});

        assertThrows(BusinessException.class,
                () -> petService.replacePetGalleryImage(1L, 7L, pdf));

        verify(imageStorageService, never())
                .uploadImage(any(), anyString());
    }

    @Test
    void shouldDeleteGalleryFilesAndRowsWhenDeletingPet() {

        pet.setImagePublicId("pub/primary");

        com.petadoption.entity.PetImage g1 = galleryImage(7L, "pub/g1");
        com.petadoption.entity.PetImage g2 = galleryImage(8L, "pub/g2");

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petImageRepository.findByPetId(1L))
                .thenReturn(List.of(g1, g2));

        petService.deletePet(1L);

        verify(petImageRepository).delete(g1);
        verify(petImageRepository).delete(g2);
        verify(imageStorageService).deleteImage("pub/g1");
        verify(imageStorageService).deleteImage("pub/g2");
        verify(imageStorageService).deleteImage("pub/primary");
        verify(petRepository).delete(pet);
    }


    @Test
    void shouldRefuseUpdateForOtherShelterAndChangeNothing() {

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        doThrow(new BusinessException("no access"))
                .when(shelterScopeService)
                .verifyShelterAccess(any(), any());

        assertThrows(BusinessException.class,
                () -> petService.updatePet(1L, requestDto));

        verify(petRepository, never()).save(any());
    }

    @Test
    void shouldRefuseDeleteForOtherShelterAndTouchNothing() {

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        doThrow(new BusinessException("no access"))
                .when(shelterScopeService)
                .verifyShelterAccess(any(), any());

        assertThrows(BusinessException.class,
                () -> petService.deletePet(1L));

        verify(petRepository, never()).delete(any(Pet.class));
        verify(imageStorageService, never()).deleteImage(anyString());
    }

    @Test
    void shouldRefuseDeleteWhenPetHasMedicalRecords() {

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(medicalRecordRepository.existsByPetId(1L)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> petService.deletePet(1L));

        verify(petRepository, never()).delete(any(Pet.class));
        verify(imageStorageService, never()).deleteImage(anyString());
    }

    @Test
    void shouldForceAvailableStatusForAnonymousVisitors() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::isAnonymous).thenReturn(true);

            var criteria = new com.petadoption.dto.request
                    .PetSearchCriteria("Dog", null, null, null, null,
                    PetStatus.ADOPTED, "beagle");

            var visible = petService.visibleCriteria(criteria);

            assertEquals(PetStatus.AVAILABLE, visible.status());
            assertEquals("Dog", visible.species());
            assertEquals("beagle", visible.search());
        }
    }

    @Test
    void shouldLeaveCriteriaUntouchedForLoggedInUsers() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::isAnonymous).thenReturn(false);

            var criteria = new com.petadoption.dto.request
                    .PetSearchCriteria(null, null, null, null, null,
                    PetStatus.ADOPTED, null);

            assertSame(criteria, petService.visibleCriteria(criteria));
        }
    }

    @Test
    void shouldHideNonAvailablePetFromAnonymousVisitors() {

        pet.setStatus(PetStatus.ADOPTED);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::isAnonymous).thenReturn(true);

            assertThrows(ResourceNotFoundException.class,
                    () -> petService.getPetById(1L));
        }
    }

    @Test
    void shouldShowNonAvailablePetToLoggedInUsers() {

        pet.setStatus(PetStatus.ADOPTED);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petMapper.toResponseDto(pet)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::isAnonymous).thenReturn(false);

            assertEquals(1L, petService.getPetById(1L).id());
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
                petService.getAllPets(pageable, EMPTY_CRITERIA);

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