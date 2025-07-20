package com.david.profile_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    TopicExchange profileEventsExchange() {
        return ExchangeBuilder.topicExchange(identityEventsExchange)
                .durable(true)
                .build();
    }

    @Bean
    Queue profileEventsQueue() {
        return new Queue(userRegisteredQueue, true);
    }

    @Bean
    Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(profileEventsQueue())
                .to(profileEventsExchange())
                .with(userRegisteredRoutingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
