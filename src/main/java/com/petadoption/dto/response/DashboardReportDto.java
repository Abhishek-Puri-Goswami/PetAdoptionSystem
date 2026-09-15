package com.petadoption.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardReportDto {

    private long totalPets;

    private long availablePets;

    private long adoptedPets;

    private long totalShelters;

    private long totalUsers;

    private long totalApplications;

    private long pendingApplications;

    private long approvedApplications;

    private long rejectedApplications;

    private long totalAppointments;
}