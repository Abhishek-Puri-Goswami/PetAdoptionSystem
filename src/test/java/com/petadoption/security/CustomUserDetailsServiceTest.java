package com.petadoption.security;

import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {

        Role role = new Role();
        role.setId(1L);
        role.setName(RoleType.ROLE_SYSTEM_ADMIN);

        user = new User();
        user.setId(1L);
        user.setFirstName("Abhishek");
        user.setLastName("Goswami");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setEnabled(true);
        user.setRoles(Set.of(role));
    }

    @Test
    void shouldLoadUserByUsername() {

        when(userRepository.findByEmail(
                "test@test.com"))
                .thenReturn(
                        Optional.of(user)
                );

        UserDetails userDetails =
                customUserDetailsService
                        .loadUserByUsername(
                                "test@test.com"
                        );

        assertNotNull(userDetails);

        assertEquals(
                "test@test.com",
                userDetails.getUsername()
        );

        assertEquals(
                "password",
                userDetails.getPassword()
        );

        assertTrue(
                userDetails.isEnabled()
        );
    }

    @Test
    void shouldMapAuthorities() {

        when(userRepository.findByEmail(
                "test@test.com"))
                .thenReturn(
                        Optional.of(user)
                );

        UserDetails details =
                customUserDetailsService
                        .loadUserByUsername(
                                "test@test.com"
                        );

        assertEquals(
                1,
                details.getAuthorities().size()
        );

        assertTrue(
                details.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals(
                                                "ROLE_SYSTEM_ADMIN"
                                        )
                        )
        );
    }

    @Test
    void shouldPreserveEnabledFlag() {

        user.setEnabled(false);

        when(userRepository.findByEmail(
                "test@test.com"))
                .thenReturn(
                        Optional.of(user)
                );

        UserDetails details =
                customUserDetailsService
                        .loadUserByUsername(
                                "test@test.com"
                        );

        assertFalse(
                details.isEnabled()
        );
    }

    @Test
    void shouldThrowUsernameNotFoundException() {

        when(userRepository.findByEmail(
                "missing@test.com"))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                UsernameNotFoundException.class,
                () ->
                        customUserDetailsService
                                .loadUserByUsername(
                                        "missing@test.com"
                                )
        );
    }

    @Test
    void shouldReturnCorrectRoleName() {

        when(userRepository.findByEmail(
                "test@test.com"))
                .thenReturn(
                        Optional.of(user)
                );

        UserDetails details =
                customUserDetailsService
                        .loadUserByUsername(
                                "test@test.com"
                        );

        String authority =
                details.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority();

        assertEquals(
                "ROLE_SYSTEM_ADMIN",
                authority
        );
    }
}