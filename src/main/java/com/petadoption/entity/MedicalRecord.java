package com.petadoption.entity;

import com.petadoption.validation.Rules;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false, length = Rules.DESCRIPTION_MAX)
    private String description;

    @Column(length = Rules.VET_NAME_MAX)
    private String vetName;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
}
