package com.petadoption.service.impl;

import com.petadoption.dto.response.DashboardReportDto;
import com.petadoption.enums.ApplicationStatus;
import com.petadoption.enums.PetStatus;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.AppointmentRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.ShelterRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.ReportService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl
        implements ReportService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;
    private final AppointmentRepository appointmentRepository;
    private final AdoptionApplicationRepository adoptionRepository;
    private final AuditLogService auditLogService;

    @Override
    public DashboardReportDto getDashboardReport() {

        return DashboardReportDto.builder()

                .totalPets(
                        petRepository.count())

                .availablePets(
                        petRepository.countByStatus(
                                PetStatus.AVAILABLE))

                .adoptedPets(
                        petRepository.countByStatus(
                                PetStatus.ADOPTED))

                .totalShelters(
                        shelterRepository.count())

                .totalUsers(
                        userRepository.count())

                .totalApplications(
                        adoptionRepository.count())

                .pendingApplications(
                        adoptionRepository.countByStatus(
                                ApplicationStatus.PENDING))

                .approvedApplications(
                        adoptionRepository.countByStatus(
                                ApplicationStatus.APPROVED))

                .rejectedApplications(
                        adoptionRepository.countByStatus(
                                ApplicationStatus.REJECTED))

                .totalAppointments(
                        appointmentRepository.count())

                .build();
    }
}