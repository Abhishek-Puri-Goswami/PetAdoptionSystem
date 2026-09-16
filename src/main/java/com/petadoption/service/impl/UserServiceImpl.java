package com.petadoption.service.impl;

import com.petadoption.dto.request.AssignShelterRequestDto;
import com.petadoption.dto.request.UpdateProfileRequestDto;
import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.entity.Role;
import com.petadoption.entity.Shelter;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.UserMapper;
import com.petadoption.repository.RoleRepository;
import com.petadoption.repository.ShelterRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.service.UserService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShelterRepository shelterRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    public PageResponseDto<UserResponseDto> getAllUsers(
            Pageable pageable) {

        Page<UserResponseDto> page =
                userRepository.findAll(pageable)
                        .map(userMapper::toResponseDto);

        return PageResponseDto.from(page);
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

    @Override
    public UserResponseDto getMyProfile() {

        User user = currentUser();

        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateMyProfile(
            UpdateProfileRequestDto request) {

        User user = currentUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setAddressLine1(request.addressLine1());
        user.setAddressLine2(request.addressLine2());
        user.setCity(request.city());
        user.setState(request.state());
        user.setPostalCode(request.postalCode());
        user.setCountry(request.country());

        User updated = userRepository.save(user);

        auditLogService.saveAuditLog(
                "USER_PROFILE_UPDATED",
                "User",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "User updated their own profile");

        return userMapper.toResponseDto(updated);
    }

    @Override
    public UserResponseDto assignShelter(
            Long userId,
            AssignShelterRequestDto request) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        if (request.shelterId() == null) {

            user.setShelter(null);

        } else {

            Shelter shelter =
                    shelterRepository.findById(request.shelterId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Shelter not found"));

            user.setShelter(shelter);
        }

        User updated = userRepository.save(user);

        auditLogService.saveAuditLog(
                "USER_SHELTER_ASSIGNED",
                "User",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                request.shelterId() == null
                        ? "User unassigned from shelter"
                        : "User assigned to shelter "
                                + request.shelterId());

        return userMapper.toResponseDto(updated);
    }

    private User currentUser() {

        String email = SecurityUtil.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}