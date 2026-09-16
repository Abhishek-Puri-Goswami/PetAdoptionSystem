package com.petadoption.service.impl;

import com.petadoption.dto.response.AppNotificationResponseDto;
import com.petadoption.entity.AppNotification;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AppNotificationMapper;
import com.petadoption.repository.AppNotificationRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AppNotificationService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements AppNotificationService {

    private final AppNotificationRepository notificationRepository;
    private final AppNotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Override
    public void notify(User recipient, String message) {

        AppNotification notification = new AppNotification();
        notification.setRecipient(recipient);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    @Override
    public List<AppNotificationResponseDto> getMyNotifications() {

        User user = currentUser();

        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(notificationMapper::toResponseDto)
                .toList();
    }

    @Override
    public void markAsRead(Long id) {

        User user = currentUser();

        AppNotification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You can only manage your own notifications");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private User currentUser() {

        String email = SecurityUtil.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}
