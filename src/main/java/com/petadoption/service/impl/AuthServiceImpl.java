package com.petadoption.service.impl;

import com.petadoption.dto.request.LoginRequestDto;
import com.petadoption.dto.request.RegisterRequestDto;
import com.petadoption.dto.response.AuthResponseDto;
import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.BusinessException;
import com.petadoption.notification.EmailService;
import com.petadoption.notification.EmailTemplateBuilder;
import com.petadoption.notification.NotificationConstants;
import com.petadoption.repository.RoleRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.security.JwtService;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Email already registered");
        }

        Role adopterRole = roleRepository.findByName(RoleType.ROLE_ADOPTER)
                .orElseThrow(() ->
                        new BusinessException("Default role not found"));

        User user = new User();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        user.getRoles().add(adopterRole);

        User savedUser = userRepository.save(user);

        auditLogService.saveAuditLog(
                "USER_REGISTERED",
                "User",
                String.valueOf(savedUser.getId()),
                savedUser.getEmail(),
                "User registered");

        emailService.sendEmail(
                savedUser.getEmail(),
                NotificationConstants.WELCOME_SUBJECT,
                EmailTemplateBuilder.welcomeEmail(
                        savedUser.getFirstName())
        );

        String token =
                jwtService.generateToken(
                        savedUser.getEmail());

        return AuthResponseDto.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .token(token)
                .build();
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(
                        request.email())
                .orElseThrow(() ->
                        new BusinessException(
                                "Invalid email or password"));

        if (!user.isEnabled()) {
            throw new BusinessException(
                    "User account is disabled");
        }

        boolean matches =
                passwordEncoder.matches(
                        request.password(),
                        user.getPassword());

        if (!matches) {

            throw new BusinessException(
                    "Invalid email or password");
        }

        String token =
                jwtService.generateToken(
                        user.getEmail());

        auditLogService.saveAuditLog(
                "USER_LOGIN",
                "User",
                String.valueOf(user.getId()),
                user.getEmail(),
                "User logged in");

        return AuthResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .token(token)
                .build();
    }
}