package com.david.profile_service.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.keycloak-events}")
    private String keycloakEventsExchange;

    @Value("${app.rabbitmq.queue.keycloak-events}")
    private String keycloakEventsQueue;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    @Bean
    DirectExchange keycloakEventsExchange() {
        return new DirectExchange(keycloakEventsExchange, true, false);
    }

    @Bean
    Queue keycloakEventsQueue() {
        return new Queue(keycloakEventsQueue, true);
    }

    @Bean
    Binding userRegisteredBinding(DirectExchange keycloakEventsExchange, Queue keycloakEventsQueue) {
        return BindingBuilder
                .bind(keycloakEventsQueue)
                .to(keycloakEventsExchange)
                .with(userRegisteredRoutingKey);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
