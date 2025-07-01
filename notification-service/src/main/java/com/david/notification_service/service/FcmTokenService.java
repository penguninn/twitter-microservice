package com.david.notification_service.service;

import com.david.notification_service.dto.FcmTokenRequest;
import com.david.notification_service.entity.FcmToken;
import com.david.notification_service.entity.Notification;
import com.david.notification_service.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void saveFcmToken(FcmTokenRequest fcmToken) {
        log.info("Saving FCM token");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("NotificationService::countUnreadNotifications - User is not authenticated");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        String userId = jwt.getSubject();
        Optional<FcmToken> existing = fcmTokenRepository.findByUserId(userId);
        if (existing.isPresent()) {
            existing.get().setToken(fcmToken.getToken());
            fcmTokenRepository.save(existing.get());
        } else {
            fcmTokenRepository.save(FcmToken.builder().userId(userId).token(fcmToken.getToken()).build());
        }
        log.info("FCM token saved successfully");
    }

    public void sendPushNotification(FcmToken fcmToken, Notification notification) {
        log.info("Sending push notification");
        Message message = Message.builder()
                .setToken(fcmToken.getToken())
                .putData("id", notification.getId())
                .putData("userId", notification.getUserId())
                .putData("senderId", notification.getSenderId())
                .putData("type", notification.getType().name())
                .putData("message", notification.getMessage())
                .putData("read", String.valueOf(notification.isRead()))
                .putData("createdAt", notification.getCreatedAt())
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending FCM message", e);
        }
    }
}
