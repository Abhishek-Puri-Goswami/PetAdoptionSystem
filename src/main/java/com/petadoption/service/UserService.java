package com.petadoption.service;

import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id);

    UserResponseDto updateRoles(
            Long userId,
            UpdateUserRoleRequestDto request);

    UserResponseDto disableUser(Long id);

    UserResponseDto enableUser(Long id);
}