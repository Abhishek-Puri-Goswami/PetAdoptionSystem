package com.petadoption.service;

import com.petadoption.dto.response.AppNotificationResponseDto;
import com.petadoption.entity.User;

import java.util.List;

public interface AppNotificationService {

    void notify(User recipient, String message);

    List<AppNotificationResponseDto> getMyNotifications();

    void markAsRead(Long id);

}
