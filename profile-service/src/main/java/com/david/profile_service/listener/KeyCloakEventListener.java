package com.david.profile_service.listener;

import com.david.profile_service.dto.keycloak_events.UserEventDto;
import com.david.profile_service.mapper.ProfileMapper;
import com.david.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyCloakEventListener {

    private final ProfileService profileService;

    @RabbitListener(queues = "${app.rabbitmq.queue.keycloak-events}")
    public void handleUserRegisteredEvent(@Payload UserEventDto message) {
        log.info("Received user registered event: {}", message);
        try {
            if (message != null && "REGISTER".equals(message.getEventType())) {
                log.info("Processing user registration for userId: {}, username: {}, email: {}",
                        message.getUserId(), message.getUsername(), message.getEmail());
                profileService.register(ProfileMapper.mapToCreationRequest(message));
            }
        } catch (Exception e) {
            log.error("Error processing user registered event: {}", e.getMessage(), e);
        }
    }

}
