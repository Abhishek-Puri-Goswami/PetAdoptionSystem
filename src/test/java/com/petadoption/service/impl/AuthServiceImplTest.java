package com.petadoption.service.impl;

import com.petadoption.dto.request.LoginRequestDto;
import com.petadoption.dto.request.RegisterRequestDto;
import com.petadoption.dto.response.AuthResponseDto;
import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.BusinessException;
import com.petadoption.notification.EmailService;
import com.petadoption.repository.RoleRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.security.JwtService;
import com.petadoption.service.AuditLogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto registerRequest;

    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {

        registerRequest =
                new RegisterRequestDto(
                        "Abhishek",
                        "Goswami",
                        "test@test.com",
                        "password"
                );

        loginRequest =
                new LoginRequestDto(
                        "test@test.com",
                        "password"
                );
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        Role role = new Role();
        role.setName(RoleType.ROLE_ADOPTER);

        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setEmail("test@test.com");
        savedUser.setFirstName("Abhishek");

        when(userRepository.findByEmail(
                registerRequest.email()))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName(
                RoleType.ROLE_ADOPTER))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtService.generateToken(
                savedUser.getEmail()))
                .thenReturn("jwt-token");

        AuthResponseDto response =
                authService.register(
                        registerRequest
                );

        assertNotNull(response);

        assertEquals(
                "test@test.com",
                response.getEmail()
        );

        assertEquals(
                "jwt-token",
                response.getToken()
        );

        verify(emailService)
                .sendEmail(
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(auditLogService)
                .saveAuditLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        User existingUser = new User();

        when(userRepository.findByEmail(
                registerRequest.email()))
                .thenReturn(
                        Optional.of(existingUser)
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.register(
                                registerRequest
                        )
                );

        assertEquals(
                "Email already registered",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenDefaultRoleMissing() {

        when(userRepository.findByEmail(
                registerRequest.email()))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName(
                RoleType.ROLE_ADOPTER))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.register(
                                registerRequest
                        )
                );

        assertEquals(
                "Default role not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldLoginSuccessfully() {

        User user = new User();

        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setEnabled(true);

        when(userRepository.findByEmail(
                loginRequest.email()))
                .thenReturn(
                        Optional.of(user)
                );

        when(passwordEncoder.matches(
                "password",
                "encoded"))
                .thenReturn(true);

        when(jwtService.generateToken(
                user.getEmail()))
                .thenReturn("jwt-token");

        AuthResponseDto response =
                authService.login(
                        loginRequest
                );

        assertNotNull(response);

        assertEquals(
                "test@test.com",
                response.getEmail()
        );

        assertEquals(
                "jwt-token",
                response.getToken()
        );

        verify(auditLogService)
                .saveAuditLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByEmail(
                loginRequest.email()))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.login(
                                loginRequest
                        )
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenAccountDisabled() {

        User user = new User();

        user.setEnabled(false);

        when(userRepository.findByEmail(
                loginRequest.email()))
                .thenReturn(
                        Optional.of(user)
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.login(
                                loginRequest
                        )
                );

        assertEquals(
                "User account is disabled",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPasswordIncorrect() {

        User user = new User();

        user.setEnabled(true);
        user.setPassword("encoded");

        when(userRepository.findByEmail(
                loginRequest.email()))
                .thenReturn(
                        Optional.of(user)
                );

        when(passwordEncoder.matches(
                "password",
                "encoded"))
                .thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.login(
                                loginRequest
                        )
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }
}