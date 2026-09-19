package com.petadoption.entity;

import com.petadoption.validation.Rules;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shelters")
@Getter
@Setter
@NoArgsConstructor
public class Shelter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Rules.SHELTER_NAME_MAX)
    private String name;

    @Column(length = Rules.EMAIL_MAX)
    private String email;

    @Column(length = Rules.PHONE_MAX)
    private String phone;

    @Column(length = Rules.ADDRESS_LINE_MAX)
    private String addressLine1;

    @Column(length = Rules.ADDRESS_LINE_MAX)
    private String addressLine2;

    @Column(length = Rules.PLACE_MAX)
    private String city;

    @Column(length = Rules.PLACE_MAX)
    private String state;

    @Column(length = Rules.POSTAL_MAX)
    private String postalCode;

    @Column(length = Rules.PLACE_MAX)
    private String country;

    @Column(length = Rules.DESCRIPTION_MAX)
    private String description;

    private String imageUrl;

    private String imagePublicId;

    @OneToMany(mappedBy = "shelter")
    private List<Pet> pets = new ArrayList<>();

}