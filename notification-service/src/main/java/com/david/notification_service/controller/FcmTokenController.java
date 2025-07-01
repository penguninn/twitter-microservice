package com.david.notification_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.notification_service.dto.FcmTokenRequest;
import com.david.notification_service.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/fcm-tokens")
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    public ApiResponse<?> saveFcmToken(FcmTokenRequest fcmToken) {
        log.info("FcmTokenService::saveFcmToken - Execution started");
        fcmTokenService.saveFcmToken(fcmToken);
        log.info("FcmTokenService::saveFcmToken - Execution completed");
        return new ApiResponse<>(HttpStatus.OK, "FCM token saved successfully");
    }

}
