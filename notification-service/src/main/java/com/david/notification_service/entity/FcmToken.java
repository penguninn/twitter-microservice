package com.david.notification_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fcm_tokens")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmToken {

    private String userId;

    private String token;
}
