package com.david.profile_service.listener;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.profile.ProfileCreationEventPayload;
import com.david.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyCloakEventListener {

    private final ProfileService profileService;

    @RabbitListener(queues = "${app.rabbitmq.queue.user-registered}")
    public void handleUserRegisteredEvent(@Payload ApiEventMessage<ProfileCreationEventPayload> payload) {
        log.info("Received user registered event: {}", payload.getEventId());
        try {
            if ("REGISTER".equals(payload.getEventType())) {
                log.info("Processing user registration for userId: {}, username: {}, email: {}",
                        payload.getPayload().getUserId(),
                        payload.getPayload().getUsername(),
                        payload.getPayload().getEmail()
                );
                profileService.register(payload.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing user registered event: {}", e.getMessage(), e);
        }
    }

}
