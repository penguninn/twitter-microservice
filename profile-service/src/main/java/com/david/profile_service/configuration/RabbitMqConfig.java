package com.david.profile_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.identity-events}")
    private String identityEventsExchange;

    @Value("${app.rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    @Bean
    TopicExchange keycloakEventsExchange() {
        return ExchangeBuilder.topicExchange(identityEventsExchange)
                .durable(true)
                .build();
    }

    @Bean
    Queue keycloakEventsQueue() {
        return new Queue(userRegisteredQueue, true);
    }

    @Bean
    Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(keycloakEventsQueue())
                .to(keycloakEventsExchange())
                .with(userRegisteredRoutingKey);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
