package com.petadoption.service.impl;

import com.petadoption.dto.response.AppNotificationResponseDto;
import com.petadoption.entity.AppNotification;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AppNotificationMapper;
import com.petadoption.repository.AppNotificationRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppNotificationServiceTest {

    @Mock
    private AppNotificationRepository notificationRepository;

    @Mock
    private AppNotificationMapper notificationMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppNotificationServiceImpl notificationService;

    @Test
    void shouldSaveNotificationForRecipient() {

        User recipient = new User();
        recipient.setId(1L);

        notificationService.notify(recipient, "Test message");

        verify(notificationRepository).save(any(AppNotification.class));
    }

    @Test
    void shouldGetMyNotifications() {

        User user = new User();
        user.setId(1L);

        AppNotification notification = new AppNotification();

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(user));

            when(notificationRepository
                    .findByRecipientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(notification));

            when(notificationMapper.toResponseDto(notification))
                    .thenReturn(mock(AppNotificationResponseDto.class));

            List<AppNotificationResponseDto> result =
                    notificationService.getMyNotifications();

            assertEquals(1, result.size());
        }
    }

    @Test
    void shouldMarkOwnNotificationAsRead() {

        User user = new User();
        user.setId(1L);

        AppNotification notification = new AppNotification();
        notification.setId(5L);
        notification.setRecipient(user);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(user));

            when(notificationRepository.findById(5L))
                    .thenReturn(Optional.of(notification));

            notificationService.markAsRead(5L);

            assertTrue(notification.isRead());

            verify(notificationRepository).save(notification);
        }
    }

    @Test
    void shouldThrowWhenMarkingOthersNotificationAsRead() {

        User currentUser = new User();
        currentUser.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        AppNotification notification = new AppNotification();
        notification.setId(5L);
        notification.setRecipient(otherUser);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(currentUser));

            when(notificationRepository.findById(5L))
                    .thenReturn(Optional.of(notification));

            assertThrows(
                    BusinessException.class,
                    () -> notificationService.markAsRead(5L));
        }
    }

    @Test
    void shouldThrowWhenMarkingMissingNotificationAsRead() {

        User user = new User();
        user.setId(1L);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("test@test.com");

            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(user));

            when(notificationRepository.findById(5L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> notificationService.markAsRead(5L));
        }
    }
}
