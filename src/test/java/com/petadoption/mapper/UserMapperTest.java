package com.petadoption.mapper;

import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.entity.Role;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;

import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper =
            Mappers.getMapper(
                    UserMapper.class
            );

    @Test
    void shouldMapUserToResponseDto() {

        Role role1 = new Role();
        role1.setId(1L);
        role1.setName(
                RoleType.ROLE_SYSTEM_ADMIN
        );

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName(
                RoleType.ROLE_ADOPTER
        );

        User user = new User();

        user.setId(100L);
        user.setFirstName("Abhishek");
        user.setLastName("Goswami");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setEnabled(true);

        user.setRoles(
                Set.of(role1, role2)
        );

        Shelter shelter = new Shelter();
        shelter.setId(7L);
        shelter.setName("Happy Paws");
        user.setShelter(shelter);

        UserResponseDto dto =
                mapper.toResponseDto(
                        user
                );

        assertNotNull(dto);

        assertEquals(
                100L,
                dto.id()
        );

        assertEquals(
                "Abhishek",
                dto.firstName()
        );

        assertEquals(
                "Goswami",
                dto.lastName()
        );

        assertEquals(
                "test@test.com",
                dto.email()
        );

        assertTrue(
                dto.enabled()
        );

        assertEquals(
                2,
                dto.roles().size()
        );

        assertTrue(
                dto.roles().contains(
                        "ROLE_SYSTEM_ADMIN"
                )
        );

        assertTrue(
                dto.roles().contains(
                        "ROLE_ADOPTER"
                )
        );

        assertEquals(
                7L,
                dto.shelterId()
        );

        assertEquals(
                "Happy Paws",
                dto.shelterName()
        );
    }

    @Test
    void shouldMapNullShelterToNullShelterFields() {

        User user = new User();
        user.setId(2L);
        user.setRoles(Set.of());

        UserResponseDto dto = mapper.toResponseDto(user);

        assertNull(dto.shelterId());
        assertNull(dto.shelterName());
    }

    @Test
    void shouldMapEmptyRoles() {

        User user = new User();

        user.setId(1L);
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEmail("user@test.com");
        user.setEnabled(true);
        user.setRoles(Set.of());

        UserResponseDto dto =
                mapper.toResponseDto(
                        user
                );

        assertNotNull(dto);

        assertTrue(
                dto.roles().isEmpty()
        );
    }
}