package com.petadoption.entity;

import com.petadoption.validation.Rules;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Rules.NAME_MAX)
    private String firstName;

    @Column(nullable = false, length = Rules.NAME_MAX)
    private String lastName;

    @Column(nullable = false, unique = true, length = Rules.EMAIL_MAX)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean enabled = true;

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

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "adopter")
    private Set<AdoptionApplication> adoptionApplications = new HashSet<>();
}