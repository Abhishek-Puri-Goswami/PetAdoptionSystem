package com.petadoption.entity;

import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;
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

    @Column(nullable = false)
    private String name;

    private String species;

    private String breed;

    private Integer age;

    private String gender;

    @Column(length = 2000)
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

    @Column(length = 1000)
    private String specialCareNotes;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @OneToMany(mappedBy = "pet")
    private List<PetImage> images = new ArrayList<>();
}