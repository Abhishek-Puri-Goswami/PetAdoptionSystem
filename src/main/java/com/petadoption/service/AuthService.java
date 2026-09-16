package com.petadoption.service;

import com.petadoption.dto.request.ForgotPasswordRequestDto;
import com.petadoption.dto.request.LoginRequestDto;
import com.petadoption.dto.request.RegisterRequestDto;
import com.petadoption.dto.request.ResetPasswordRequestDto;
import com.petadoption.dto.response.AuthResponseDto;

public interface AuthService {

    AuthResponseDto register(
            RegisterRequestDto request);

    AuthResponseDto login(
            LoginRequestDto request);

    void forgotPassword(ForgotPasswordRequestDto request);

    void resetPassword(ResetPasswordRequestDto request);

}
