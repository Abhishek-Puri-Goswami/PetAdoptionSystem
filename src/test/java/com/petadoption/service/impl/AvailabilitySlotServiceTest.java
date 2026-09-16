package com.petadoption.service.impl;

import com.petadoption.dto.request.AvailabilitySlotRequestDto;
import com.petadoption.dto.response.AvailabilitySlotResponseDto;
import com.petadoption.entity.AvailabilitySlot;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AvailabilitySlotMapper;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.AvailabilitySlotRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ShelterScopeService;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilitySlotServiceTest {

    @Mock
    private AvailabilitySlotRepository slotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilitySlotMapper slotMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ShelterScopeService shelterScopeService;

    @InjectMocks
    private AvailabilitySlotServiceImpl slotService;

    private Shelter shelter;
    private User shelterAdmin;

    @BeforeEach
    void setUp() {

        shelter = new Shelter();
        shelter.setId(1L);
        shelter.setName("Happy Paws");

        shelterAdmin = new User();
        shelterAdmin.setId(2L);
        shelterAdmin.setShelter(shelter);
    }

    @Test
    void shouldCreateSlot() {

        AvailabilitySlotRequestDto request =
                new AvailabilitySlotRequestDto(
                        LocalDateTime.now().plusDays(1));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(shelterScopeService.requireOwnShelterId(shelterAdmin))
                .thenReturn(1L);

        when(slotRepository.existsByShelterIdAndSlotDateTime(
                1L, request.slotDateTime()))
                .thenReturn(false);

        AvailabilitySlot saved = new AvailabilitySlot();
        saved.setId(5L);

        when(slotRepository.save(any()))
                .thenReturn(saved);

        when(slotMapper.toResponseDto(saved))
                .thenReturn(mock(AvailabilitySlotResponseDto.class));

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("shelteradmin@test.com");

            AvailabilitySlotResponseDto result =
                    slotService.createSlot(request);

            assertNotNull(result);

            verify(slotRepository).save(any());
        }
    }

    @Test
    void shouldThrowWhenSlotAlreadyExistsAtSameDateTime() {

        AvailabilitySlotRequestDto request =
                new AvailabilitySlotRequestDto(
                        LocalDateTime.now().plusDays(1));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(shelterScopeService.requireOwnShelterId(shelterAdmin))
                .thenReturn(1L);

        when(slotRepository.existsByShelterIdAndSlotDateTime(
                1L, request.slotDateTime()))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> slotService.createSlot(request));

        verify(slotRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCreatingSlotWithoutShelter() {

        AvailabilitySlotRequestDto request =
                new AvailabilitySlotRequestDto(
                        LocalDateTime.now().plusDays(1));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(shelterScopeService.requireOwnShelterId(shelterAdmin))
                .thenThrow(new BusinessException(
                        "You must be assigned to a shelter to "
                                + "perform this action"));

        assertThrows(
                BusinessException.class,
                () -> slotService.createSlot(request));

        verify(slotRepository, never()).save(any());
    }

    @Test
    void shouldReturnOpenSlotsForShelter() {

        AvailabilitySlot openSlot = new AvailabilitySlot();
        openSlot.setId(1L);

        when(slotRepository
                .findByShelterIdAndBookedFalseAndSlotDateTimeAfter(
                        eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(openSlot));

        when(slotMapper.toResponseDto(openSlot))
                .thenReturn(mock(AvailabilitySlotResponseDto.class));

        List<AvailabilitySlotResponseDto> result =
                slotService.getOpenSlotsForShelter(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnOwnShelterSlotsForShelterAdmin() {

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(shelterScopeService.isSystemAdmin(shelterAdmin))
                .thenReturn(false);

        when(shelterScopeService.requireOwnShelterId(shelterAdmin))
                .thenReturn(1L);

        when(slotRepository.findByShelterId(1L))
                .thenReturn(List.of());

        slotService.getMySlots();

        verify(slotRepository).findByShelterId(1L);
        verify(slotRepository, never()).findAll();
    }

    @Test
    void shouldReturnAllSlotsForSystemAdmin() {

        User systemAdmin = new User();
        systemAdmin.setId(99L);

        when(shelterScopeService.currentUser())
                .thenReturn(systemAdmin);

        when(shelterScopeService.isSystemAdmin(systemAdmin))
                .thenReturn(true);

        when(slotRepository.findAll())
                .thenReturn(List.of());

        slotService.getMySlots();

        verify(slotRepository).findAll();
        verify(slotRepository, never()).findByShelterId(any());
    }

    @Test
    void shouldDeleteUnbookedSlot() {

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setShelter(shelter);
        slot.setBooked(false);

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(slot));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("shelteradmin@test.com");

            slotService.deleteSlot(1L);

            verify(slotRepository).delete(slot);
        }
    }

    @Test
    void shouldThrowWhenDeletingBookedSlot() {

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setShelter(shelter);
        slot.setBooked(true);

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(slot));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        assertThrows(
                BusinessException.class,
                () -> slotService.deleteSlot(1L));

        verify(slotRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingSlotWithAppointmentHistory() {

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setShelter(shelter);
        slot.setBooked(false);

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(slot));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        when(appointmentRepository.existsBySlotId(1L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> slotService.deleteSlot(1L));

        verify(slotRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingNonexistentSlot() {

        when(slotRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> slotService.deleteSlot(1L));
    }

    @Test
    void shouldDenyDeletingSlotFromAnotherShelter() {

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setShelter(shelter);
        slot.setBooked(false);

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(slot));

        when(shelterScopeService.currentUser())
                .thenReturn(shelterAdmin);

        doThrow(new BusinessException(
                "You do not have access to this shelter's data"))
                .when(shelterScopeService)
                .verifyShelterAccess(shelterAdmin, 1L);

        assertThrows(
                BusinessException.class,
                () -> slotService.deleteSlot(1L));

        verify(slotRepository, never()).delete(any());
    }
}
