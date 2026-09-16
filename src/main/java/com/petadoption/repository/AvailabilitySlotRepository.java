package com.petadoption.repository;

import com.petadoption.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilitySlotRepository
        extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByShelterId(Long shelterId);

    List<AvailabilitySlot> findByShelterIdAndBookedFalseAndSlotDateTimeAfter(
            Long shelterId, LocalDateTime after);

    boolean existsByShelterIdAndSlotDateTime(
            Long shelterId, LocalDateTime slotDateTime);
}
