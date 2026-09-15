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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ReportServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AdoptionApplicationRepository adoptionRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGenerateDashboardReport() {

        when(petRepository.count()).thenReturn(10L);

        when(petRepository.countByStatus(
                PetStatus.AVAILABLE))
                .thenReturn(7L);

        when(petRepository.countByStatus(
                PetStatus.ADOPTED))
                .thenReturn(3L);

        when(shelterRepository.count()).thenReturn(2L);

        when(userRepository.count()).thenReturn(5L);

        when(adoptionRepository.count()).thenReturn(4L);

        when(adoptionRepository.countByStatus(
                ApplicationStatus.PENDING))
                .thenReturn(1L);

        when(adoptionRepository.countByStatus(
                ApplicationStatus.APPROVED))
                .thenReturn(2L);

        when(adoptionRepository.countByStatus(
                ApplicationStatus.REJECTED))
                .thenReturn(1L);

        when(appointmentRepository.count()).thenReturn(8L);

        DashboardReportDto result =
                reportService.getDashboardReport();

        assertThat(result.getTotalPets())
                .isEqualTo(10);

        assertThat(result.getAvailablePets())
                .isEqualTo(7);

        assertThat(result.getAdoptedPets())
                .isEqualTo(3);

        assertThat(result.getTotalShelters())
                .isEqualTo(2);

        assertThat(result.getTotalUsers())
                .isEqualTo(5);

        assertThat(result.getTotalApplications())
                .isEqualTo(4);

        assertThat(result.getPendingApplications())
                .isEqualTo(1);

        assertThat(result.getApprovedApplications())
                .isEqualTo(2);

        assertThat(result.getRejectedApplications())
                .isEqualTo(1);

        assertThat(result.getTotalAppointments())
                .isEqualTo(8);
    }
}