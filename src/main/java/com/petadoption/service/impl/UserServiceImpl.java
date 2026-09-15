package com.petadoption.service.impl;

import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.UserMapper;
import com.petadoption.repository.RoleRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.UserService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    public List<UserResponseDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateRoles(
            Long userId,
            UpdateUserRoleRequestDto request) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        Set<Role> roles =
                request.roles()
                        .stream()
                        .map(RoleType::valueOf)
                        .map(roleType ->
                                roleRepository.findByName(roleType)
                                        .orElseThrow(() ->
                                                new ResourceNotFoundException(
                                                        "Role not found")))
                        .collect(Collectors.toSet());

        user.setRoles(roles);
        User updated = userRepository.save(user);

        auditLogService.saveAuditLog(
                "USER_ROLES_UPDATED",
                "User",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "User roles updated");

        return userMapper.toResponseDto(updated);
    }

    @Override
    public UserResponseDto disableUser(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        user.setEnabled(false);

        User updated =
                userRepository.save(user);

        auditLogService.saveAuditLog(
                "USER_DISABLED",
                "User",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "User disabled");

        return userMapper.toResponseDto(updated);
    }

    @Override
    public UserResponseDto enableUser(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        user.setEnabled(true);

        User updated =
                userRepository.save(user);

        auditLogService.saveAuditLog(
                "USER_ENABLED",
                "User",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "User enabled");

        return userMapper.toResponseDto(updated);
    }
}