package com.petadoption.service.impl;

import com.petadoption.dto.request.AppointmentRequestDto;
import com.petadoption.dto.response.AppointmentResponseDto;
import com.petadoption.entity.Appointment;
import com.petadoption.entity.AvailabilitySlot;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.AppointmentStatus;
import com.petadoption.enums.PetStatus;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AppointmentMapper;
import com.petadoption.notification.EmailService;
import com.petadoption.notification.EmailTemplateBuilder;
import com.petadoption.notification.NotificationConstants;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.AvailabilitySlotRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AppointmentService;

import com.petadoption.service.AuditLogService;
import com.petadoption.service.ShelterScopeService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final PetRepository petRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final ShelterScopeService shelterScopeService;

    @Override
    @Transactional
    public AppointmentResponseDto createAppointment(
            AppointmentRequestDto requestDto) {

        Pet pet =
                petRepository.findById(requestDto.petId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Pet not found"));

        AvailabilitySlot slot =
                slotRepository.findById(requestDto.slotId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Slot not found"));

        if (slot.isBooked()) {
            throw new BusinessException(
                    "This slot has already been booked");
        }

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new BusinessException(
                    "Pet is not available for an appointment");
        }

        String email =
                SecurityUtil.getCurrentUserEmail();

        User adopter =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Adopter not found"));

        slot.setBooked(true);
        slotRepository.save(slot);

        Appointment appointment = new Appointment();

        appointment.setPet(pet);
        appointment.setShelter(slot.getShelter());
        appointment.setSlot(slot);
        appointment.setAdopter(adopter);

        appointment.setAppointmentDateTime(
                slot.getSlotDateTime());

        appointment.setNotes(
                requestDto.notes());

        appointment.setStatus(
                AppointmentStatus.PENDING);

        Appointment saved =
                appointmentRepository.save(appointment);

        auditLogService.saveAuditLog(
                "APPOINTMENT_CREATED",
                "Appointment",
                String.valueOf(saved.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Appointment created");

        return appointmentMapper.toResponseDto(saved);
    }

    @Override
    public AppointmentResponseDto getAppointmentById(Long id) {

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment not found with id: " + id));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                shelterIdOf(appointment));

        return appointmentMapper.toResponseDto(appointment);
    }

    @Override
    public List<AppointmentResponseDto> getAllAppointments() {

        User caller = shelterScopeService.currentUser();

        List<Appointment> appointments =
                shelterScopeService.isSystemAdmin(caller)
                        ? appointmentRepository.findAll()
                        : appointmentRepository.findByShelterId(
                                shelterScopeService
                                        .requireOwnShelterId(caller));

        return appointments
                .stream()
                .map(appointmentMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<AppointmentResponseDto>
    getMyAppointments() {

        String email =
                SecurityUtil.getCurrentUserEmail();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        return appointmentRepository
                .findByAdopterId(user.getId())
                .stream()
                .map(appointmentMapper::toResponseDto)
                .toList();
    }

    @Override
    public AppointmentResponseDto getMyAppointmentById(Long id) {

        String email =
                SecurityUtil.getCurrentUserEmail();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment not found"));

        if (!appointment.getAdopter()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You can only view your own appointment");
        }

        return appointmentMapper.toResponseDto(
                appointment);
    }

    @Override
    public AppointmentResponseDto approveAppointment(Long id) {

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment not found with id: " + id));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                shelterIdOf(appointment));

        Pet pet = appointment.getPet();

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new BusinessException(
                    "Pet is no longer available for this appointment");
        }

        pet.setStatus(PetStatus.PENDING_ADOPTION);
        petRepository.save(pet);

        appointment.setStatus(
                AppointmentStatus.APPROVED);

        Appointment updated =
                appointmentRepository.save(appointment);

        auditLogService.saveAuditLog(
                "APPOINTMENT_APPROVED",
                "Appointment",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Appointment approved");

        auditLogService.saveAuditLog(
                "PET_STATUS_PENDING_ADOPTION",
                "Pet",
                String.valueOf(pet.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Pet marked pending adoption after appointment approval");

        emailService.sendEmail(
                appointment.getAdopter().getEmail(),
                NotificationConstants.APPOINTMENT_APPROVED_SUBJECT,
                EmailTemplateBuilder.appointmentApproved(
                        appointment.getAdopter().getFirstName(),
                        appointment.getPet().getName())
        );

        return appointmentMapper.toResponseDto(updated);
    }

    @Override
    public AppointmentResponseDto rejectAppointment(Long id) {

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment not found with id: " + id));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                shelterIdOf(appointment));

        Pet pet = appointment.getPet();

        if (pet.getStatus() == PetStatus.PENDING_ADOPTION) {
            pet.setStatus(PetStatus.AVAILABLE);
            petRepository.save(pet);

            auditLogService.saveAuditLog(
                    "PET_STATUS_AVAILABLE",
                    "Pet",
                    String.valueOf(pet.getId()),
                    SecurityUtil.getCurrentUserEmail(),
                    "Pet returned to available after appointment rejection");
        }

        if (appointment.getSlot() != null
                && appointment.getSlot().isBooked()) {

            AvailabilitySlot slot = appointment.getSlot();
            slot.setBooked(false);
            slotRepository.save(slot);
        }

        appointment.setStatus(
                AppointmentStatus.REJECTED);

        Appointment updated =
                appointmentRepository.save(appointment);

        auditLogService.saveAuditLog(
                "APPOINTMENT_REJECTED",
                "Appointment",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Appointment rejected");

        emailService.sendEmail(
                appointment.getAdopter().getEmail(),
                NotificationConstants.APPOINTMENT_REJECTED_SUBJECT,
                EmailTemplateBuilder.appointmentRejected(
                        appointment.getAdopter().getFirstName(),
                        appointment.getPet().getName())
        );

        return appointmentMapper.toResponseDto(updated);
    }

    @Override
    public AppointmentResponseDto completeAppointment(Long id) {

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment not found with id: " + id));

        shelterScopeService.verifyShelterAccess(
                shelterScopeService.currentUser(),
                shelterIdOf(appointment));

        appointment.setStatus(
                AppointmentStatus.COMPLETED);

        Appointment updated =
                appointmentRepository.save(appointment);

        auditLogService.saveAuditLog(
                "APPOINTMENT_COMPLETED",
                "Appointment",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Appointment completed");

        emailService.sendEmail(
                appointment.getAdopter().getEmail(),
                NotificationConstants.APPOINTMENT_COMPLETED_SUBJECT,
                EmailTemplateBuilder.appointmentCompleted(
                        appointment.getAdopter().getFirstName(),
                        appointment.getPet().getName())
        );

        return appointmentMapper.toResponseDto(updated);
    }

    private Long shelterIdOf(Appointment appointment) {
        return appointment.getShelter() != null
                ? appointment.getShelter().getId()
                : null;
    }
}