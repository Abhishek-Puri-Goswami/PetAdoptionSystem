package com.petadoption.service;

import com.petadoption.dto.request.UpdateProfileRequestDto;
import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.UserResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponseDto<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserById(Long id);

    UserResponseDto updateRoles(
            Long userId,
            UpdateUserRoleRequestDto request);

    UserResponseDto disableUser(Long id);

    UserResponseDto enableUser(Long id);

    UserResponseDto getMyProfile();

    UserResponseDto updateMyProfile(UpdateProfileRequestDto request);
}