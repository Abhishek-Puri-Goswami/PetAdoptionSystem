package com.petadoption.service.impl;

import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.AppointmentResponseDto;
import com.petadoption.entity.Appointment;
import com.petadoption.entity.AvailabilitySlot;
import com.petadoption.entity.Pet;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.enums.AppointmentStatus;
import com.petadoption.enums.PetStatus;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AppointmentMapper;
import com.petadoption.notification.EmailService;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.AvailabilitySlotRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private AvailabilitySlotRepository slotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ShelterScopeService shelterScopeService;

    @Mock
    private com.petadoption.service.AppNotificationService
            appNotificationService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

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
    void shouldCreateAppointment() {

        AppointmentRequestDto request =
                new AppointmentRequestDto(
                        1L,
                        1L,
                        "Visit"
                );

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setStatus(PetStatus.AVAILABLE);

        Shelter shelter = new Shelter();
        shelter.setId(1L);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setShelter(shelter);
        slot.setSlotDateTime(LocalDateTime.now().plusDays(1));
        slot.setBooked(false);

        User adopter = new User();
        adopter.setEmail("test@test.com");

        Appointment appointment =
                new Appointment();

        appointment.setId(10L);

        AppointmentResponseDto response =
                new AppointmentResponseDto(
                        10L,
                        1L,
                        1L,
                        1L,
                        1L,
                        LocalDateTime.now(),
                        "Visit",
                        "PENDING"
                );

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(petRepository.findById(1L))
                    .thenReturn(Optional.of(pet));

            when(slotRepository.findById(1L))
                    .thenReturn(Optional.of(slot));

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(adopter));

            when(appointmentRepository.save(any()))
                    .thenReturn(appointment);

            when(appointmentMapper.toResponseDto(
                    appointment))
                    .thenReturn(response);

            AppointmentResponseDto result =
                    appointmentService.createAppointment(
                            request);

            assertThat(result).isNotNull();

            assertThat(slot.isBooked()).isTrue();

            verify(slotRepository).save(slot);

            verify(appointmentRepository)
                    .save(any());
        }
    }

    @Test
    void shouldThrowWhenPetNotFound() {

        AppointmentRequestDto request =
                new AppointmentRequestDto(
                        1L,
                        1L,
                        "Visit"
                );

        when(petRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                appointmentService.createAppointment(
                        request))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenSlotNotFound() {

        AppointmentRequestDto request =
                new AppointmentRequestDto(
                        1L,
                        1L,
                        "Visit"
                );

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setStatus(PetStatus.AVAILABLE);

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(slotRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                appointmentService.createAppointment(
                        request))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenSlotAlreadyBooked() {

        AppointmentRequestDto request =
                new AppointmentRequestDto(
                        1L,
                        1L,
                        "Visit"
                );

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setStatus(PetStatus.AVAILABLE);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setBooked(true);

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(slot));

        assertThatThrownBy(() ->
                appointmentService.createAppointment(
                        request))
                .isInstanceOf(BusinessException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldGetMyAppointments() {

        User user = new User();
        user.setId(1L);

        Appointment appointment =
                new Appointment();

        AppointmentResponseDto response =
                mock(AppointmentResponseDto.class);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(user));

            when(appointmentRepository.findByAdopterId(
                    1L))
                    .thenReturn(List.of(appointment));

            when(appointmentMapper.toResponseDto(
                    appointment))
                    .thenReturn(response);

            List<AppointmentResponseDto> result =
                    appointmentService.getMyAppointments();

            assertThat(result).hasSize(1);
        }
    }

    @Test
    void shouldThrowWhenViewingOtherUsersAppointment() {

        User currentUser = new User();
        currentUser.setId(1L);

        User otherUser = new User();
        otherUser.setId(99L);

        Appointment appointment =
                new Appointment();

        appointment.setAdopter(otherUser);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail(
                    "test@test.com"))
                    .thenReturn(Optional.of(currentUser));

            when(appointmentRepository.findById(1L))
                    .thenReturn(Optional.of(appointment));

            assertThatThrownBy(() ->
                    appointmentService.getMyAppointmentById(
                            1L))
                    .isInstanceOf(
                            BusinessException.class);
        }
    }

    @Test
    void shouldApproveAppointment() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");

        Appointment appointment =
                new Appointment();

        appointment.setAdopter(adopter);
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(
                appointment))
                .thenReturn(mock(
                        AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.approveAppointment(1L);

            assertThat(appointment.getStatus())
                    .isEqualTo(
                            AppointmentStatus.APPROVED);

            verify(emailService)
                    .sendEmail(
                            anyString(),
                            anyString(),
                            anyString());
        }
    }

    @Test
    void shouldRejectAppointment() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");

        Appointment appointment =
                new Appointment();

        appointment.setAdopter(adopter);
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(
                appointment))
                .thenReturn(mock(
                        AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.rejectAppointment(1L);

            assertThat(appointment.getStatus())
                    .isEqualTo(
                            AppointmentStatus.REJECTED);
        }
    }

    @Test
    void shouldCompleteAppointment() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");

        Appointment appointment =
                new Appointment();

        appointment.setAdopter(adopter);
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(
                appointment))
                .thenReturn(mock(
                        AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.completeAppointment(1L);

            assertThat(appointment.getStatus())
                    .isEqualTo(
                            AppointmentStatus.COMPLETED);
        }
    }

    @Test
    void shouldScopeAppointmentsToOwnShelterWhenNotSystemAdmin() {

        User shelterAdmin = new User();
        shelterAdmin.setId(2L);

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(shelterScopeService.isSystemAdmin(shelterAdmin))
                .thenReturn(false);

        when(shelterScopeService.requireOwnShelterId(shelterAdmin))
                .thenReturn(7L);

        when(appointmentRepository.findByShelterId(7L))
                .thenReturn(List.of());

        appointmentService.getAllAppointments();

        verify(appointmentRepository)
                .findByShelterId(7L);

        verify(appointmentRepository, never())
                .findAll();
    }

    @Test
    void shouldVerifyShelterAccessWhenGettingAppointmentById() {

        Shelter shelter = new Shelter();
        shelter.setId(3L);

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setShelter(shelter);

        User caller = new User();
        caller.setId(2L);

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentMapper.toResponseDto(appointment))
                .thenReturn(mock(AppointmentResponseDto.class));

        appointmentService.getAppointmentById(1L);

        verify(shelterScopeService)
                .verifyShelterAccess(caller, 3L);
    }

    @Test
    void shouldRejectAppointmentAccessWhenShelterAccessDenied() {

        Appointment appointment = new Appointment();
        appointment.setId(1L);

        User caller = new User();

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        doThrow(new BusinessException(
                "You do not have access to this shelter's data"))
                .when(shelterScopeService)
                .verifyShelterAccess(eq(caller), any());

        assertThatThrownBy(() ->
                appointmentService.getAppointmentById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldMarkPetPendingAdoptionWhenApprovingAppointment() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");
        pet.setStatus(PetStatus.AVAILABLE);

        Appointment appointment = new Appointment();
        appointment.setAdopter(adopter);
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(appointment))
                .thenReturn(mock(AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.approveAppointment(1L);

            assertThat(pet.getStatus())
                    .isEqualTo(PetStatus.PENDING_ADOPTION);

            verify(petRepository).save(pet);
        }
    }

    @Test
    void shouldThrowWhenApprovingAppointmentForUnavailablePet() {

        Pet pet = new Pet();
        pet.setStatus(PetStatus.ADOPTED);

        Appointment appointment = new Appointment();
        appointment.setAdopter(new User());
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            assertThatThrownBy(() ->
                    appointmentService.approveAppointment(1L))
                    .isInstanceOf(BusinessException.class);

            verify(appointmentRepository, never()).save(any());
        }
    }

    @Test
    void shouldReturnPetToAvailableWhenRejectingAppointmentThatWasPendingAdoption() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");
        pet.setStatus(PetStatus.PENDING_ADOPTION);

        Appointment appointment = new Appointment();
        appointment.setAdopter(adopter);
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(appointment))
                .thenReturn(mock(AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.rejectAppointment(1L);

            assertThat(pet.getStatus())
                    .isEqualTo(PetStatus.AVAILABLE);

            verify(petRepository).save(pet);
        }
    }

    @Test
    void shouldNotChangePetStatusWhenRejectingAppointmentForNonPendingPet() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");
        pet.setStatus(PetStatus.AVAILABLE);

        Appointment appointment = new Appointment();
        appointment.setAdopter(adopter);
        appointment.setPet(pet);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(appointment))
                .thenReturn(mock(AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.rejectAppointment(1L);

            assertThat(pet.getStatus())
                    .isEqualTo(PetStatus.AVAILABLE);

            verify(petRepository, never()).save(any());
        }
    }

    @Test
    void shouldReleaseSlotWhenRejectingAppointment() {

        User adopter = new User();
        adopter.setEmail("test@test.com");
        adopter.setFirstName("Abhishek");

        Pet pet = new Pet();
        pet.setName("Buddy");
        pet.setStatus(PetStatus.PENDING_ADOPTION);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(5L);
        slot.setBooked(true);

        Appointment appointment = new Appointment();
        appointment.setAdopter(adopter);
        appointment.setPet(pet);
        appointment.setSlot(slot);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        when(appointmentMapper.toResponseDto(appointment))
                .thenReturn(mock(AppointmentResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("admin@test.com");

            appointmentService.rejectAppointment(1L);

            assertThat(slot.isBooked()).isFalse();

            verify(slotRepository).save(slot);
        }
    }
}