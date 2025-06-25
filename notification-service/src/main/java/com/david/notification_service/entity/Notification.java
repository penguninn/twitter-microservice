package com.david.notification_service.entity;

import com.david.notification_service.enums.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    private String id;

    private String userId;

    private String senderId;

    private TypeNotification type;

    private String message;

    private boolean read;

    private String createdAt;
}
