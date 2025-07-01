package com.david.notification_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FcmTokenRequest {

    private String token;
}
