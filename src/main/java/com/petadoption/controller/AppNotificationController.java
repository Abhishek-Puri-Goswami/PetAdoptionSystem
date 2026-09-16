package com.petadoption.controller;

import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.dto.response.AppNotificationResponseDto;
import com.petadoption.service.AppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class AppNotificationController {

    private final AppNotificationService notificationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ApiResponseDto<List<AppNotificationResponseDto>>
    getMyNotifications() {

        return new ApiResponseDto<>(
                true,
                "Notifications fetched successfully",
                notificationService.getMyNotifications()
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/read")
    public ApiResponseDto<String> markAsRead(@PathVariable Long id) {

        notificationService.markAsRead(id);

        return new ApiResponseDto<>(
                true,
                "Notification marked as read",
                null
        );
    }
}
