package com.petadoption.service;

import com.petadoption.entity.Role;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShelterScopeServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShelterScopeService shelterScopeService;

    @Test
    void shouldReturnCurrentUser() {

        User user = new User();
        user.setEmail("staff@shelter.com");

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("staff@shelter.com");

            when(userRepository.findByEmail("staff@shelter.com"))
                    .thenReturn(Optional.of(user));

            assertEquals(user, shelterScopeService.currentUser());
        }
    }

    @Test
    void shouldThrowWhenCurrentUserMissing() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("ghost@test.com");

            when(userRepository.findByEmail("ghost@test.com"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    shelterScopeService::currentUser);
        }
    }

    @Test
    void shouldIdentifySystemAdmin() {

        Role role = new Role();
        role.setName(RoleType.ROLE_SYSTEM_ADMIN);

        User user = new User();
        user.setRoles(Set.of(role));

        assertTrue(shelterScopeService.isSystemAdmin(user));
    }

    @Test
    void shouldNotIdentifyNonSystemAdminAsSystemAdmin() {

        Role role = new Role();
        role.setName(RoleType.ROLE_SHELTER_ADMIN);

        User user = new User();
        user.setRoles(Set.of(role));

        assertFalse(shelterScopeService.isSystemAdmin(user));
    }

    @Test
    void shouldReturnOwnShelterId() {

        Shelter shelter = new Shelter();
        shelter.setId(5L);

        User user = new User();
        user.setShelter(shelter);

        assertEquals(5L, shelterScopeService.requireOwnShelterId(user));
    }

    @Test
    void shouldThrowWhenNoShelterAssigned() {

        User user = new User();
        user.setRoles(Set.of());

        assertThrows(
                BusinessException.class,
                () -> shelterScopeService.requireOwnShelterId(user));
    }

    @Test
    void shouldAllowSystemAdminAccessToAnyShelter() {

        Role role = new Role();
        role.setName(RoleType.ROLE_SYSTEM_ADMIN);

        User user = new User();
        user.setRoles(Set.of(role));

        assertDoesNotThrow(() ->
                shelterScopeService.verifyShelterAccess(user, 999L));
    }

    @Test
    void shouldAllowAccessToOwnShelter() {

        Shelter shelter = new Shelter();
        shelter.setId(5L);

        Role role = new Role();
        role.setName(RoleType.ROLE_SHELTER_ADMIN);

        User user = new User();
        user.setRoles(Set.of(role));
        user.setShelter(shelter);

        assertDoesNotThrow(() ->
                shelterScopeService.verifyShelterAccess(user, 5L));
    }

    @Test
    void shouldDenyAccessToOtherShelter() {

        Shelter shelter = new Shelter();
        shelter.setId(5L);

        Role role = new Role();
        role.setName(RoleType.ROLE_SHELTER_ADMIN);

        User user = new User();
        user.setRoles(Set.of(role));
        user.setShelter(shelter);

        assertThrows(
                BusinessException.class,
                () -> shelterScopeService.verifyShelterAccess(user, 6L));
    }

    @Test
    void shouldDenyAccessWhenCallerHasNoShelter() {

        Role role = new Role();
        role.setName(RoleType.ROLE_SHELTER_ADMIN);

        User user = new User();
        user.setRoles(Set.of(role));

        assertThrows(
                BusinessException.class,
                () -> shelterScopeService.verifyShelterAccess(user, 6L));
    }
}
