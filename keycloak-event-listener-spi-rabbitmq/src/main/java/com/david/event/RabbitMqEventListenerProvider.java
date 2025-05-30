package com.david.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import com.david.dto.UserEventDto;
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

                UserEventDto userEventDto = new UserEventDto(userId, username, email, event.getType().name());

                try {
                    String message = objectMapper.writeValueAsString(userEventDto);
                    try (var connection = factory.newConnection();
                         var channel = connection.createChannel()) {
                        channel.exchangeDeclare(exchangeName, "direct", true);
                        channel.basicPublish(exchangeName, routingKeyUserRegistered, null, message.getBytes(StandardCharsets.UTF_8));
                        System.out.println(" [Keycloak SPI] Sent '" + routingKeyUserRegistered + "':'" + message + "'");
                    } catch (Exception e) {
                        System.err.println(" [Keycloak SPI] CRITICAL: Error publishing message to RabbitMQ for user ID: " + userEventDto.getUserId() +
                                ", Event Type: " + userEventDto.getEventType() + ". Error: " + e.getMessage());
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
