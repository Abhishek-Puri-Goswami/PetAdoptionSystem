package com.petadoption.ai.tool;

public record ApplicationStatusSummary(

        String petName,

        String status,

        String reviewNotes,

        String rejectionReason
) {
}
