package com.petadoption.entity;

import lombok.Data;

import com.petadoption.validation.Rules;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "availability_slots")
public class AvailabilitySlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime slotDateTime;

    private boolean booked = false;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;
}
