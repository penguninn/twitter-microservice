package com.david.comment_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.comment-events}")
    private String commentEventsExchange;

    @Value("${app.rabbitmq.queue.comment-created}")
    private String commentCreatedQueue;
    @Value("${app.rabbitmq.queue.comment-deleted}")
    private String commentDeletedQueue;

    @Value("${app.rabbitmq.routing-key.comment-created}")
    private String commentCreatedRoutingKey;
    @Value("${app.rabbitmq.routing-key.comment-deleted}")
    private String commentDeletedRoutingKey;


    @Bean
    public TopicExchange commentEventsExchange() {
        return ExchangeBuilder.topicExchange(commentEventsExchange)
                .durable(true)
                .build();
    }

    // Comment created
    @Bean
    public Queue commentCreatedQueue() {
        return QueueBuilder.durable(commentCreatedQueue)
                .build();
    }

    @Bean
    public Binding commentCreatedBinding() {
        return BindingBuilder.bind(commentCreatedQueue())
                .to(commentEventsExchange())
                .with(commentCreatedRoutingKey);
    }

    // Comment deleted
    @Bean
    public Queue commentDeletedQueue() {
        return QueueBuilder.durable(commentDeletedQueue)
                .build();
    }

    @Bean
    public Binding commentDeletedBinding() {
        return BindingBuilder.bind(commentDeletedQueue())
                .to(commentEventsExchange())
                .with(commentDeletedRoutingKey);
    }

}
