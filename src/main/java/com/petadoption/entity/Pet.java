package com.petadoption.entity;

import com.petadoption.enums.PetStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;
}