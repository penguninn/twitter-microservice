package com.david.profile_service.listener;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.profile.ProfileCreatedEventPayload;
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
    public void handleUserRegisteredEvent(@Payload ApiEventMessage<ProfileCreatedEventPayload> message) {
        log.info("Received user registered event: {}", message.getEventId());
        try {
            if ("REGISTER".equals(message.getEventType())) {
                log.info("Processing user registration for userId: {}, username: {}, email: {}",
                        message.getPayload().getUserId(),
                        message.getPayload().getUsername(),
                        message.getPayload().getEmail()
                );
                profileService.register(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing user registered event: {}", e.getMessage(), e);
        }
        log.info("User registered event processed successfully: {}", message.getEventId());
    }

}
