package com.petadoption.entity;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;
import com.petadoption.validation.Rules;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Rules.PET_NAME_MAX)
    private String name;

    @Column(length = Rules.SPECIES_MAX)
    private String species;

    @Column(length = Rules.PET_BREED_MAX)
    private String breed;

    private Integer age;

    @Column(length = Rules.GENDER_MAX)
    private String gender;

    @Column(length = Rules.DESCRIPTION_MAX)
    private String description;

    @Enumerated(EnumType.STRING)
    private PetStatus status = PetStatus.AVAILABLE;

    private String imageUrl;

    private String imagePublicId;

    @Enumerated(EnumType.STRING)
    private EnergyLevel energyLevel;

    @Enumerated(EnumType.STRING)
    private Temperament temperament;

    private boolean sterilized;

    @Column(length = Rules.NOTES_MAX)
    private String specialCareNotes;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @OneToMany(mappedBy = "pet")
    private List<PetImage> images = new ArrayList<>();
}