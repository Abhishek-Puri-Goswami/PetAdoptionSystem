package com.petadoption.controller;

import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponseDto<List<UserResponseDto>> getAllUsers() {

        return new ApiResponseDto<>(
                true,
                "Users fetched successfully",
                userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ApiResponseDto<UserResponseDto> getUserById(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "User fetched successfully",
                userService.getUserById(id));
    }

    @PutMapping("/{id}/roles")
    public ApiResponseDto<UserResponseDto> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Roles updated successfully",
                userService.updateRoles(id, request));
    }

    @PutMapping("/{id}/disable")
    public ApiResponseDto<UserResponseDto> disableUser(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "User disabled successfully",
                userService.disableUser(id));
    }

    @PutMapping("/{id}/enable")
    public ApiResponseDto<UserResponseDto> enableUser(
            @PathVariable Long id) {

        return new ApiResponseDto<>(
                true,
                "User enabled successfully",
                userService.enableUser(id));
    }
}