package com.david.notification_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<?> getNotifications(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("NotificationController::getNotifications - Execution started");
        var notifications = notificationService.getNotifications(page, size, sortBy);
        log.info("NotificationController::getNotifications - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Fetched notifications successfully", notifications);
    }

    @GetMapping("/unread/count")
    public ApiResponse<?> getUnreadNotificationCount() {
        log.info("NotificationController::getUnreadNotificationCount - Execution started");
        long count = notificationService.countUnreadNotifications();
        log.info("NotificationController::getUnreadNotificationCount - Execution ended with count: {}", count);
        return new ApiResponse<>(HttpStatus.OK, "Fetched unread notification count successfully", count);
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<?> markNotificationAsRead(@PathVariable String notificationId) {
        log.info("NotificationController::markNotificationAsRead - Execution started");
        notificationService.markNotificationAsRead(notificationId);
        log.info("NotificationController::markNotificationAsRead - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Marked notification as read successfully");
    }

    @PatchMapping("/read-all")
    public ApiResponse<?> markAllNotificationsAsRead() {
        log.info("NotificationController::markAllNotificationsAsRead - Execution started");
        notificationService.markAllNotificationsAsRead();
        log.info("NotificationController::markAllNotificationsAsRead - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Marked all notifications as read successfully");
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<?> deleteNotification(@PathVariable String notificationId) {
        log.info("NotificationController::deleteNotification - Execution started");
        notificationService.deleteNotification(notificationId);
        log.info("NotificationController::deleteNotification - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Deleted notification successfully");
    }

    @DeleteMapping
    public ApiResponse<?> deleteNotifications(@RequestParam(required = false) Boolean read) {
        log.info("NotificationController::deleteNotifications - Execution started with read = {}", read);
        notificationService.deleteNotifications(read);
        log.info("NotificationController::deleteNotifications - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Deleted notifications successfully");
    }
}
