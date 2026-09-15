package com.petadoption.service.impl;

import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.ApplicationStatus;
import com.petadoption.enums.PetStatus;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AdoptionMapper;
import com.petadoption.notification.EmailService;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

    @InjectMocks
    private AdoptionServiceImpl adoptionService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
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

        when(adoptionRepository.findAll())
                .thenReturn(List.of(application));

        when(adoptionMapper.toResponseDto(application))
                .thenReturn(dto);

        List<AdoptionResponseDto> result =
                adoptionService.getAllApplications();

        assertThat(result)
                .hasSize(1);
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