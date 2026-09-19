package com.petadoption.entity;

import com.petadoption.enums.AppointmentStatus;
import lombok.Data;

import com.petadoption.validation.Rules;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime appointmentDateTime;

    @Column(length = Rules.NOTES_MAX)
    private String notes;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "adopter_id")
    private User adopter;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    private AvailabilitySlot slot;
}