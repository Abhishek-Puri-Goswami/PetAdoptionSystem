package com.petadoption.entity;

import com.petadoption.enums.ApplicationStatus;
import com.petadoption.validation.Rules;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adoption_applications")
@Getter
@Setter
@NoArgsConstructor
public class AdoptionApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = Rules.APPLICANT_NOTES_MAX)
    private String applicantNotes;

    @Column(length = Rules.NOTES_MAX)
    private String livingSituation;

    @Column(length = Rules.NOTES_MAX)
    private String priorPetExperience;

    @Column(length = Rules.NOTES_MAX)
    private String householdDetails;

    @Column(length = Rules.PREFERRED_CONTACT_MAX)
    private String preferredContact;

    @Column(length = Rules.NOTES_MAX)
    private String reviewNotes;

    @Column(length = Rules.NOTES_MAX)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "adopter_id", nullable = false)
    private User adopter;

}