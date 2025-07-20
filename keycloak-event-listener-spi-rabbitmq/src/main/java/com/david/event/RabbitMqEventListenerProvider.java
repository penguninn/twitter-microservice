package com.david.event;

import com.david.common.dto.profile.ProfileCreatedEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.nio.charset.StandardCharsets;

public class RabbitMqEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final ConnectionFactory factory;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String exchangeName;
    private final String routingKeyUserRegistered;

    public RabbitMqEventListenerProvider(KeycloakSession session, String rabbitHost, int rabbitPort, String rabbitUser, String rabbitPassword, String virtualHost, String exchangeName, String routingKeyUserRegistered) {
        this.session = session;
        this.factory = new ConnectionFactory();
        this.factory.setHost(rabbitHost);
        this.factory.setPort(rabbitPort);
        this.factory.setUsername(rabbitUser);
        this.factory.setPassword(rabbitPassword);
        this.factory.setVirtualHost(virtualHost);
        this.exchangeName = exchangeName;
        this.routingKeyUserRegistered = routingKeyUserRegistered;
    }


    @Override
    public void onEvent(Event event) {
        if(event.getType().equals(EventType.REGISTER)) {
            RealmModel realmModel = session.realms().getRealm(event.getRealmId());
            UserModel userModel = session.users().getUserById(realmModel, event.getUserId());

            if(userModel != null) {
                String userId = userModel.getId();
                String username = userModel.getUsername();
                String email = userModel.getEmail();
                String displayName = userModel.getFirstAttribute("name");
                String profileImgUrl = userModel.getFirstAttribute("avatar");

                EventListener<?> userRegisteredMessage = EventListener.builder()
                        .eventId(event.getId())
                        .eventType(event.getType().toString())
                        .timestamp(event.getTime())
                        .payload(ProfileCreatedEventPayload.builder()
                                .userId(userId)
                                .username(username)
                                .email(email)
                                .displayName(displayName)
                                .profileImageUrl(profileImgUrl)
                                .build())
                        .build();
                try {
                    String message = objectMapper.writeValueAsString(userRegisteredMessage);
                    try (var connection = factory.newConnection();
                         var channel = connection.createChannel()) {
                        channel.exchangeDeclare(exchangeName, "topic", true);
                        channel.basicPublish(exchangeName, routingKeyUserRegistered, null, message.getBytes(StandardCharsets.UTF_8));
                        System.out.println(" [Keycloak SPI] Sent '" + routingKeyUserRegistered + "':'" + message + "'");
                    } catch (Exception e) {
                        System.err.println(" [Keycloak SPI] CRITICAL: Error publishing message to RabbitMQ for event ID: " + userRegisteredMessage.getEventId() +
                                ", Event Type: " + userRegisteredMessage.getEventType() + ". Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }
}
