package com.petadoption.controller;

import com.petadoption.dto.request.ForgotPasswordRequestDto;
import com.petadoption.dto.request.LoginRequestDto;
import com.petadoption.dto.request.RegisterRequestDto;
import com.petadoption.dto.request.ResetPasswordRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.AuthResponseDto;
import com.petadoption.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponseDto<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "User registered successfully",
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ApiResponseDto<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Login successful",
                authService.login(request)
        );
    }

    @PostMapping("/forgot-password")
    public ApiResponseDto<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        authService.forgotPassword(request);

        return new ApiResponseDto<>(
                true,
                "If that email is registered, a reset link has "
                        + "been sent",
                null
        );
    }

    @PostMapping("/reset-password")
    public ApiResponseDto<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        authService.resetPassword(request);

        return new ApiResponseDto<>(
                true,
                "Password reset successfully",
                null
        );
    }
}