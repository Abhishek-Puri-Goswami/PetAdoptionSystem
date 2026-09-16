package com.petadoption.service.impl;

import com.petadoption.dto.request.ForgotPasswordRequestDto;
import com.petadoption.dto.request.LoginRequestDto;
import com.petadoption.dto.request.RegisterRequestDto;
import com.petadoption.dto.request.ResetPasswordRequestDto;
import com.petadoption.dto.response.AuthResponseDto;
import com.petadoption.entity.PasswordResetToken;
import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.BusinessException;
import com.petadoption.notification.EmailService;
import com.petadoption.repository.PasswordResetTokenRepository;
import com.petadoption.repository.RoleRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.security.JwtService;
import com.petadoption.service.AuditLogService;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

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

    @Test
    void shouldCreateResetTokenAndSendEmailWhenEmailExists() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setFirstName("Abhishek");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        authService.forgotPassword(
                new ForgotPasswordRequestDto("test@test.com"));

        verify(passwordResetTokenRepository)
                .save(any(PasswordResetToken.class));

        verify(emailService)
                .sendEmail(
                        eq("test@test.com"),
                        anyString(),
                        anyString());
    }

    @Test
    void shouldDoNothingSilentlyWhenForgotPasswordEmailNotFound() {

        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        authService.forgotPassword(
                new ForgotPasswordRequestDto("missing@test.com"));

        verify(passwordResetTokenRepository, never())
                .save(any());

        verify(emailService, never())
                .sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldResetPasswordWithValidToken() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("valid-token");
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        when(passwordResetTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("encoded-new-password");

        authService.resetPassword(
                new ResetPasswordRequestDto(
                        "valid-token", "newPassword123"));

        assertEquals(
                "encoded-new-password",
                user.getPassword());

        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    void shouldThrowWhenResetTokenNotFound() {

        when(passwordResetTokenRepository.findByToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> authService.resetPassword(
                        new ResetPasswordRequestDto(
                                "bad-token", "newPassword123")));
    }

    @Test
    void shouldThrowAndDeleteWhenResetTokenExpired() {

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("expired-token");
        resetToken.setExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(passwordResetTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(resetToken));

        assertThrows(
                BusinessException.class,
                () -> authService.resetPassword(
                        new ResetPasswordRequestDto(
                                "expired-token", "newPassword123")));

        verify(passwordResetTokenRepository).delete(resetToken);
        verify(userRepository, never()).save(any());
    }
}