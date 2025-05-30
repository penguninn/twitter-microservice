package com.david.event;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RabbitMqEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String PROVIDER_ID = "rabbitmq-event-listener";
    private String rabbitHost;
    private int rabbitPort;
    private String rabbitUser;
    private String rabbitPassword;
    private String virtualHost;
    private String exchangeName;
    private String routingKeyUserRegistered;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new RabbitMqEventListenerProvider(
                keycloakSession,
                rabbitHost,
                rabbitPort,
                rabbitUser,
                rabbitPassword,
                virtualHost,
                exchangeName,
                routingKeyUserRegistered
        );
    }

    @Override
    public void init(Config.Scope scope) {
        rabbitHost = scope.get("host");
        rabbitPort = scope.getInt("port");
        rabbitUser = scope.get("username");
        rabbitPassword = scope.get("password");
        virtualHost = scope.get("virtual-host");
        exchangeName = scope.get("exchange-name");
        routingKeyUserRegistered = scope.get("routing-key-user-registered");
        System.out.println(" [Keycloak SPI] RabbitMQ Event Listener Provider initialized with host: " + rabbitHost + ", port: " + rabbitPort + ", user: " + rabbitUser + ", exchange: " + exchangeName + ", routing key: " + routingKeyUserRegistered);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
