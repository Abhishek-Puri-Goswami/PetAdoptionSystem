package com.petadoption.service.impl;

import com.petadoption.dto.request.AvailabilitySlotRequestDto;
import com.petadoption.dto.response.AvailabilitySlotResponseDto;
import com.petadoption.entity.AvailabilitySlot;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AvailabilitySlotMapper;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.AvailabilitySlotRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.AvailabilitySlotService;
import com.petadoption.service.ShelterScopeService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilitySlotServiceImpl implements AvailabilitySlotService {

    private final AvailabilitySlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotMapper slotMapper;
    private final AuditLogService auditLogService;
    private final ShelterScopeService shelterScopeService;

    @Override
    public AvailabilitySlotResponseDto createSlot(
            AvailabilitySlotRequestDto request) {

        User caller = shelterScopeService.currentUser();
        Long shelterId = shelterScopeService.requireOwnShelterId(caller);

        if (slotRepository.existsByShelterIdAndSlotDateTime(
                shelterId, request.slotDateTime())) {

            throw new BusinessException(
                    "A slot already exists at that date and time");
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setShelter(caller.getShelter());
        slot.setSlotDateTime(request.slotDateTime());

        AvailabilitySlot saved = slotRepository.save(slot);

        auditLogService.saveAuditLog(
                "AVAILABILITY_SLOT_CREATED",
                "AvailabilitySlot",
                String.valueOf(saved.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Availability slot created");

        return slotMapper.toResponseDto(saved);
    }

    @Override
    public List<AvailabilitySlotResponseDto> getOpenSlotsForShelter(
            Long shelterId) {

        return slotRepository
                .findByShelterIdAndBookedFalseAndSlotDateTimeAfter(
                        shelterId, LocalDateTime.now())
                .stream()
                .map(slotMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<AvailabilitySlotResponseDto> getMySlots() {

        User caller = shelterScopeService.currentUser();

        List<AvailabilitySlot> slots =
                shelterScopeService.isSystemAdmin(caller)
                        ? slotRepository.findAll()
                        : slotRepository.findByShelterId(
                                shelterScopeService
                                        .requireOwnShelterId(caller));

        return slots.stream()
                .map(slotMapper::toResponseDto)
                .toList();
    }

    @Override
    public void deleteSlot(Long id) {

        AvailabilitySlot slot =
                slotRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Slot not found"));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                slot.getShelter() != null
                        ? slot.getShelter().getId()
                        : null);

        if (slot.isBooked()) {
            throw new BusinessException(
                    "Cannot delete a slot that has already been booked");
        }

        if (appointmentRepository.existsBySlotId(id)) {
            throw new BusinessException(
                    "Cannot delete a slot with existing appointment "
                            + "history");
        }

        slotRepository.delete(slot);

        auditLogService.saveAuditLog(
                "AVAILABILITY_SLOT_DELETED",
                "AvailabilitySlot",
                String.valueOf(id),
                SecurityUtil.getCurrentUserEmail(),
                "Availability slot deleted");
    }
}
