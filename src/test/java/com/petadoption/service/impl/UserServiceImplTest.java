package com.petadoption.service.impl;

import com.petadoption.dto.request.UpdateProfileRequestDto;
import com.petadoption.dto.request.UpdateUserRoleRequestDto;
import com.petadoption.dto.response.PageResponseDto;
import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.UserMapper;
import com.petadoption.repository.RoleRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AuditLogService;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetAllUsers() {

        User user = new User();

        UserResponseDto response =
                new UserResponseDto(
                        1L,
                        "Abhishek",
                        "Goswami",
                        "test@test.com",
                        true,
                        Set.of("ROLE_ADOPTER"),
                        null, null, null, null, null, null, null);

        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        when(userMapper.toResponseDto(user))
                .thenReturn(response);

        PageResponseDto<UserResponseDto> result =
                userService.getAllUsers(pageable);

        assertThat(result.content())
                .hasSize(1);
    }

    @Test
    void shouldGetUserById() {

        User user = new User();
        user.setId(1L);

        UserResponseDto response =
                new UserResponseDto(
                        1L,
                        "Abhishek",
                        "Goswami",
                        "test@test.com",
                        true,
                        Set.of("ROLE_ADOPTER"),
                        null, null, null, null, null, null, null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDto(user))
                .thenReturn(response);

        UserResponseDto result =
                userService.getUserById(1L);

        assertThat(result)
                .isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getUserById(1L))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }

    @Test
    void shouldDisableUser() {

        User user = new User();
        user.setId(1L);
        user.setEnabled(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(
                        new UserResponseDto(
                                1L,
                                "Abhishek",
                                "Goswami",
                                "test@test.com",
                                false,
                                Set.of("ROLE_ADOPTER"),
                                null, null, null, null, null, null, null));

        userService.disableUser(1L);

        assertThat(user.isEnabled())
                .isFalse();

        verify(auditLogService)
                .saveAuditLog(
                        eq("USER_DISABLED"),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldEnableUser() {

        User user = new User();
        user.setId(1L);
        user.setEnabled(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(
                        new UserResponseDto(
                                1L,
                                "Abhishek",
                                "Goswami",
                                "test@test.com",
                                true,
                                Set.of("ROLE_ADOPTER"),
                                null, null, null, null, null, null, null));

        userService.enableUser(1L);

        assertThat(user.isEnabled())
                .isTrue();

        verify(auditLogService)
                .saveAuditLog(
                        eq("USER_ENABLED"),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldUpdateRoles() {

        User user = new User();
        user.setId(1L);

        Role role = new Role();
        role.setName(RoleType.ROLE_SYSTEM_ADMIN);

        UpdateUserRoleRequestDto request =
                new UpdateUserRoleRequestDto(
                        Set.of("ROLE_SYSTEM_ADMIN"));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByName(
                RoleType.ROLE_SYSTEM_ADMIN))
                .thenReturn(Optional.of(role));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(
                        new UserResponseDto(
                                1L,
                                "Abhishek",
                                "Goswami",
                                "test@test.com",
                                true,
                                Set.of("ROLE_SYSTEM_ADMIN"),
                                null, null, null, null, null, null, null));

        UserResponseDto result =
                userService.updateRoles(
                        1L,
                        request);

        assertThat(result)
                .isNotNull();

        verify(auditLogService)
                .saveAuditLog(
                        eq("USER_ROLES_UPDATED"),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldGetMyProfile() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        UserResponseDto response =
                new UserResponseDto(
                        1L,
                        "Abhishek",
                        "Goswami",
                        "test@test.com",
                        true,
                        Set.of("ROLE_ADOPTER"),
                        null, null, null, null, null, null, null);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(user));

            when(userMapper.toResponseDto(user))
                    .thenReturn(response);

            UserResponseDto result =
                    userService.getMyProfile();

            assertThat(result)
                    .isEqualTo(response);
        }
    }

    @Test
    void shouldUpdateMyProfile() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        UpdateProfileRequestDto request =
                new UpdateProfileRequestDto(
                        "Abhishek",
                        "Goswami",
                        "9876543210",
                        "12 MG Road",
                        null,
                        "Bangalore",
                        "Karnataka",
                        "560001",
                        "India");

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(user));

            when(userRepository.save(any(User.class)))
                    .thenReturn(user);

            when(userMapper.toResponseDto(user))
                    .thenReturn(
                            new UserResponseDto(
                                    1L,
                                    "Abhishek",
                                    "Goswami",
                                    "test@test.com",
                                    true,
                                    Set.of("ROLE_ADOPTER"),
                                    "9876543210",
                                    "12 MG Road",
                                    null,
                                    "Bangalore",
                                    "Karnataka",
                                    "560001",
                                    "India"));

            UserResponseDto result =
                    userService.updateMyProfile(request);

            assertThat(user.getPhone())
                    .isEqualTo("9876543210");

            assertThat(result.city())
                    .isEqualTo("Bangalore");

            verify(auditLogService)
                    .saveAuditLog(
                            eq("USER_PROFILE_UPDATED"),
                            any(),
                            any(),
                            any(),
                            any());
        }
    }

    @Test
    void shouldThrowWhenGettingMyProfileAndUserMissing() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("ghost@test.com");

            when(userRepository.findByEmail("ghost@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.getMyProfile())
                    .isInstanceOf(
                            ResourceNotFoundException.class);
        }
    }
}