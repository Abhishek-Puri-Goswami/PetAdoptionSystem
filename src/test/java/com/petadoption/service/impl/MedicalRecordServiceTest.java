package com.petadoption.service.impl;

import com.petadoption.dto.request.MedicalRecordRequestDto;
import com.petadoption.dto.response.MedicalRecordResponseDto;
import com.petadoption.entity.MedicalRecord;
import com.petadoption.entity.Pet;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.MedicalRecordMapper;
import com.petadoption.repository.MedicalRecordRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.ShelterScopeService;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @Mock
    private com.petadoption.service.AuditLogService auditLogService;

    @Mock
    private ShelterScopeService shelterScopeService;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    @Test
    void shouldCreateRecordWhenCallerHasShelterAccess() {

        Shelter shelter = new Shelter();
        shelter.setId(1L);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setShelter(shelter);

        User caller = new User();
        caller.setId(2L);

        MedicalRecordRequestDto request =
                new MedicalRecordRequestDto(
                        LocalDate.of(2026, 1, 1),
                        "Annual checkup",
                        "Dr. Smith");

        MedicalRecord saved = new MedicalRecord();
        saved.setId(10L);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("staff@shelter.com");

            when(petRepository.findById(1L))
                    .thenReturn(Optional.of(pet));

            when(shelterScopeService.currentUser())
                    .thenReturn(caller);

            when(medicalRecordRepository.save(any()))
                    .thenReturn(saved);

            when(medicalRecordMapper.toResponseDto(saved))
                    .thenReturn(mock(MedicalRecordResponseDto.class));

            MedicalRecordResponseDto result =
                    medicalRecordService.createRecord(1L, request);

            assertNotNull(result);

            verify(shelterScopeService)
                    .verifyShelterAccess(caller, 1L);
        }
    }

    @Test
    void shouldThrowWhenCreatingRecordForMissingPet() {

        MedicalRecordRequestDto request =
                new MedicalRecordRequestDto(
                        LocalDate.of(2026, 1, 1),
                        "Annual checkup",
                        "Dr. Smith");

        when(petRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> medicalRecordService.createRecord(1L, request));
    }

    @Test
    void shouldDenyCreatingRecordWhenShelterAccessDenied() {

        Pet pet = new Pet();
        pet.setId(1L);

        User caller = new User();

        MedicalRecordRequestDto request =
                new MedicalRecordRequestDto(
                        LocalDate.of(2026, 1, 1),
                        "Annual checkup",
                        "Dr. Smith");

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        doThrow(new BusinessException(
                "You do not have access to this shelter's data"))
                .when(shelterScopeService)
                .verifyShelterAccess(eq(caller), any());

        assertThrows(
                BusinessException.class,
                () -> medicalRecordService.createRecord(1L, request));

        verify(medicalRecordRepository, never()).save(any());
    }

    @Test
    void shouldGetRecordsForPetWhenCallerHasShelterAccess() {

        Pet pet = new Pet();
        pet.setId(1L);

        User caller = new User();

        MedicalRecord record = new MedicalRecord();

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        when(medicalRecordRepository.findByPetId(1L))
                .thenReturn(List.of(record));

        when(medicalRecordMapper.toResponseDto(record))
                .thenReturn(mock(MedicalRecordResponseDto.class));

        List<MedicalRecordResponseDto> result =
                medicalRecordService.getRecordsForPet(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldDenyGettingRecordsWhenShelterAccessDenied() {

        Pet pet = new Pet();
        pet.setId(1L);

        User caller = new User();

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(shelterScopeService.currentUser())
                .thenReturn(caller);

        doThrow(new BusinessException(
                "You do not have access to this shelter's data"))
                .when(shelterScopeService)
                .verifyShelterAccess(eq(caller), any());

        assertThrows(
                BusinessException.class,
                () -> medicalRecordService.getRecordsForPet(1L));
    }
}
