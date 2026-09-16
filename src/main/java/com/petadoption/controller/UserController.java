package com.petadoption.controller;

import com.petadoption.dto.request.AssignShelterRequestDto;
import com.petadoption.dto.request.UpdateProfileRequestDto;
import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {

    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ApiResponseDto<UserResponseDto> getMyProfile() {

        return new ApiResponseDto<>(
                true,
                "Profile fetched successfully",
                userService.getMyProfile());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ApiResponseDto<UserResponseDto> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Profile updated successfully",
                userService.updateMyProfile(request));
    }

    @GetMapping
    public ApiResponseDto<PageResponseDto<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        return new ApiResponseDto<>(
                true,
                "Users fetched successfully",
                userService.getAllUsers(pageable));
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

    @PutMapping("/{id}/shelter")
    public ApiResponseDto<UserResponseDto> assignShelter(
            @PathVariable Long id,
            @RequestBody AssignShelterRequestDto request) {

        return new ApiResponseDto<>(
                true,
                "Shelter assignment updated successfully",
                userService.assignShelter(id, request));
    }
}