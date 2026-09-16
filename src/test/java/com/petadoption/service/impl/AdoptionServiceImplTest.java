package com.petadoption.service.impl;

import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.ApplicationStatus;
import com.petadoption.enums.PetStatus;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AdoptionMapper;
import com.petadoption.notification.EmailService;
import com.petadoption.entity.Shelter;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ShelterScopeService;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdoptionServiceImplTest {

    @Mock
    private AdoptionApplicationRepository adoptionRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdoptionMapper adoptionMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ShelterScopeService shelterScopeService;

    @InjectMocks
    private AdoptionServiceImpl adoptionService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        User systemAdmin = new User();
        systemAdmin.setId(999L);

        when(shelterScopeService.currentUser())
                .thenReturn(systemAdmin);

        when(shelterScopeService.isSystemAdmin(systemAdmin))
                .thenReturn(true);
    }

    @Test
    void shouldCreateApplication() {

        AdoptionRequestDto request =
                new AdoptionRequestDto(
                        1L,
                        "Interested");

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setStatus(PetStatus.AVAILABLE);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        AdoptionApplication application =
                new AdoptionApplication();

        application.setId(10L);

        AdoptionResponseDto response =
                new AdoptionResponseDto(
                        10L,
                        1L,
                        "Buddy",
                        1L,
                        "test@test.com",
                        "Interested",
                        ApplicationStatus.PENDING
                );

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(petRepository.findById(1L))
                    .thenReturn(Optional.of(pet));

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(user));

            when(adoptionRepository.save(
                    any(AdoptionApplication.class)))
                    .thenReturn(application);

            when(adoptionMapper.toResponseDto(application))
                    .thenReturn(response);

            AdoptionResponseDto result =
                    adoptionService.createApplication(
                            request);

            assertThat(result)
                    .isNotNull();

            verify(adoptionRepository)
                    .save(any());
        }
    }

    @Test
    void shouldThrowWhenPetNotFound() {

        AdoptionRequestDto request =
                new AdoptionRequestDto(
                        1L,
                        "Interested");

        when(petRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                adoptionService.createApplication(
                        request))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenPetAlreadyAdopted() {

        AdoptionRequestDto request =
                new AdoptionRequestDto(
                        1L,
                        "Interested");

        Pet pet = new Pet();
        pet.setStatus(PetStatus.ADOPTED);

        User user = new User();
        user.setEmail("test@test.com");

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(petRepository.findById(1L))
                    .thenReturn(Optional.of(pet));

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    adoptionService.createApplication(
                            request))
                    .isInstanceOf(
                            BusinessException.class);
        }
    }

    @Test
    void shouldApproveApplication() {

        Pet pet = new Pet();
        pet.setStatus(PetStatus.AVAILABLE);

        User adopter = new User();
        adopter.setFirstName("Abhishek");
        adopter.setEmail("test@test.com");

        AdoptionApplication application =
                new AdoptionApplication();

        application.setPet(pet);
        application.setAdopter(adopter);

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(adoptionRepository.save(any()))
                .thenReturn(application);

        when(adoptionMapper.toResponseDto(application))
                .thenReturn(mock(AdoptionResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            adoptionService.approveApplication(1L);

            assertThat(application.getStatus())
                    .isEqualTo(
                            ApplicationStatus.APPROVED);

            assertThat(pet.getStatus())
                    .isEqualTo(
                            PetStatus.ADOPTED);

            verify(emailService)
                    .sendEmail(
                            anyString(),
                            anyString(),
                            anyString());
        }
    }

    @Test
    void shouldRejectApplication() {

        User adopter = new User();
        adopter.setFirstName("Abhishek");
        adopter.setEmail("test@test.com");

        AdoptionApplication application =
                new AdoptionApplication();

        application.setAdopter(adopter);

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(adoptionRepository.save(any()))
                .thenReturn(application);

        when(adoptionMapper.toResponseDto(application))
                .thenReturn(mock(AdoptionResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            adoptionService.rejectApplication(1L);

            assertThat(application.getStatus())
                    .isEqualTo(
                            ApplicationStatus.REJECTED);

            verify(emailService)
                    .sendEmail(
                            anyString(),
                            anyString(),
                            anyString());
        }
    }

    @Test
    void shouldGetAllApplications() {

        AdoptionApplication application =
                new AdoptionApplication();

        AdoptionResponseDto dto =
                mock(AdoptionResponseDto.class);

        Pageable pageable = PageRequest.of(0, 20);

        when(adoptionRepository.findAll(pageable))
                .thenReturn(
                        new PageImpl<>(
                                List.of(application), pageable, 1));

        when(adoptionMapper.toResponseDto(application))
                .thenReturn(dto);

        PageResponseDto<AdoptionResponseDto> result =
                adoptionService.getAllApplications(pageable);

        assertThat(result.content())
                .hasSize(1);
    }

    @Test
    void shouldScopeApplicationsToOwnShelterWhenNotSystemAdmin() {

        User shelterAdmin = new User();
        shelterAdmin.setId(2L);

        Pageable pageable = PageRequest.of(0, 20);

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(shelterScopeService.isSystemAdmin(shelterAdmin))
                .thenReturn(false);

        when(shelterScopeService.requireOwnShelterId(shelterAdmin))
                .thenReturn(7L);

        when(adoptionRepository.findByPet_Shelter_Id(7L, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        adoptionService.getAllApplications(pageable);

        verify(adoptionRepository)
                .findByPet_Shelter_Id(7L, pageable);

        verify(adoptionRepository, never())
                .findAll(any(Pageable.class));
    }

    @Test
    void shouldVerifyShelterAccessWhenGettingApplicationById() {

        Shelter shelter = new Shelter();
        shelter.setId(3L);

        Pet pet = new Pet();
        pet.setShelter(shelter);

        AdoptionApplication application = new AdoptionApplication();
        application.setId(1L);
        application.setPet(pet);

        User caller = new User();
        caller.setId(2L);

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(adoptionMapper.toResponseDto(application))
                .thenReturn(mock(AdoptionResponseDto.class));

        adoptionService.getApplicationById(1L);

        verify(shelterScopeService)
                .verifyShelterAccess(caller, 3L);
    }

    @Test
    void shouldRejectApplicationAccessWhenShelterAccessDenied() {

        Pet pet = new Pet();

        AdoptionApplication application = new AdoptionApplication();
        application.setId(1L);
        application.setPet(pet);

        User caller = new User();

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.of(application));

        doThrow(new BusinessException(
                "You do not have access to this shelter's data"))
                .when(shelterScopeService)
                .verifyShelterAccess(eq(caller), any());

        assertThatThrownBy(() ->
                adoptionService.getApplicationById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldGetMyApplications() {

        User user = new User();
        user.setId(1L);

        AdoptionApplication application =
                new AdoptionApplication();

        AdoptionResponseDto dto =
                mock(AdoptionResponseDto.class);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(
                            SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(user));

            when(adoptionRepository
                    .findByAdopterId(1L))
                    .thenReturn(List.of(application));

            when(adoptionMapper
                    .toResponseDto(application))
                    .thenReturn(dto);

            List<AdoptionResponseDto> result =
                    adoptionService
                            .getMyApplications();

            assertThat(result)
                    .hasSize(1);
        }
    }

    @Test
    void shouldThrowWhenCurrentUserNotFound() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(
                            SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(
                    () -> adoptionService
                            .getMyApplications())
                    .isInstanceOf(
                            ResourceNotFoundException.class);
        }
    }

    @Test
    void shouldGetOwnApplication() {

        User user = new User();
        user.setId(1L);

        AdoptionApplication application =
                new AdoptionApplication();

        User adopter = new User();
        adopter.setId(1L);

        application.setAdopter(adopter);

        AdoptionResponseDto dto =
                mock(AdoptionResponseDto.class);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(
                            SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(user));

            when(adoptionRepository.findById(1L))
                    .thenReturn(Optional.of(application));

            when(adoptionMapper.toResponseDto(
                    application))
                    .thenReturn(dto);

            AdoptionResponseDto result =
                    adoptionService
                            .getMyApplicationById(1L);

            assertThat(result).isNotNull();
        }
    }

    @Test
    void shouldThrowWhenViewingOthersApplication() {

        User currentUser = new User();
        currentUser.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        AdoptionApplication application =
                new AdoptionApplication();

        application.setAdopter(otherUser);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(
                            SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(currentUser));

            when(adoptionRepository.findById(1L))
                    .thenReturn(Optional.of(application));

            assertThatThrownBy(
                    () -> adoptionService
                            .getMyApplicationById(1L))
                    .isInstanceOf(
                            BusinessException.class);
        }
    }

    @Test
    void shouldGetApplicationById() {

        AdoptionApplication application =
                new AdoptionApplication();

        AdoptionResponseDto dto =
                mock(AdoptionResponseDto.class);

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(adoptionMapper
                .toResponseDto(application))
                .thenReturn(dto);

        AdoptionResponseDto result =
                adoptionService
                        .getApplicationById(1L);

        assertThat(result)
                .isNotNull();
    }

    @Test
    void shouldThrowWhenApproveApplicationNotFound() {

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> adoptionService
                        .approveApplication(1L))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenRejectApplicationNotFound() {

        when(adoptionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> adoptionService
                        .rejectApplication(1L))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }
}