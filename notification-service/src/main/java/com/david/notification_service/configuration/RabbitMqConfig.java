package com.david.notification_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.identity-events}")
    private String identityEventsExchange;
    @Value("${app.rabbitmq.exchange.follow-events}")
    private String followEventsExchange;
    @Value("${app.rabbitmq.exchange.tweet-events}")
    private String tweetEventsExchange;
    @Value("${app.rabbitmq.exchange.comment-events}")
    private String commentEventsExchange;

    @Value("${app.rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;
    @Value("${app.rabbitmq.queue.followed}")
    private String followedQueue;
    @Value("${app.rabbitmq.queue.tweet-created}")
    private String tweetCreatedQueue;
    @Value("${app.rabbitmq.queue.tweet-liked}")
    private String tweetLikedQueue;
    @Value("${app.rabbitmq.queue.comment-created}")
    private String commentCreatedQueue;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;
    @Value("${app.rabbitmq.routing-key.followed}")
    private String followedRoutingKey;
    @Value("${app.rabbitmq.routing-key.tweet-created}")
    private String tweetCreatedRoutingKey;
    @Value("${app.rabbitmq.routing-key.tweet-liked}")
    private String tweetLikedRoutingKey;
    @Value("${app.rabbitmq.routing-key.comment-created}")
    private String commentCreatedRoutingKey;

    // Exchanges
    @Bean
    public TopicExchange identityEventsExchange() {
        return ExchangeBuilder.topicExchange(identityEventsExchange)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange followEventsExchange() {
        return ExchangeBuilder.topicExchange(followEventsExchange)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange tweetEventsExchange() {
        return ExchangeBuilder.topicExchange(tweetEventsExchange)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange commentEventsExchange() {
        return ExchangeBuilder.topicExchange(commentEventsExchange)
                .durable(true)
                .build();
    }

    // Queues
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder
                .durable(userRegisteredQueue)
                .build();
    }

    @Bean
    public Queue followedQueue() {
        return QueueBuilder
                .durable(followedQueue)
                .build();
    }

    @Bean
    public Queue tweetCreatedQueue() {
        return QueueBuilder
                .durable(tweetCreatedQueue)
                .build();
    }

    @Bean
    public Queue tweetLikedQueue() {
        return QueueBuilder
                .durable(tweetLikedQueue)
                .build();
    }

    @Bean
    public Queue commentCreatedQueue() {
        return QueueBuilder
                .durable(commentCreatedQueue)
                .build();
    }

    // Bindings
    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(userRegisteredQueue())
                .to(identityEventsExchange())
                .with(userRegisteredRoutingKey);
    }

    @Bean
    public Binding followedBinding() {
        return BindingBuilder
                .bind(followedQueue())
                .to(followEventsExchange())
                .with(followedRoutingKey);
    }

    @Bean
    public Binding tweetCreatedBinding() {
        return BindingBuilder
                .bind(tweetCreatedQueue())
                .to(tweetEventsExchange())
                .with(tweetCreatedRoutingKey);
    }

    @Bean
    public Binding tweetLikedBinding() {
        return BindingBuilder
                .bind(tweetLikedQueue())
                .to(tweetEventsExchange())
                .with(tweetLikedRoutingKey);
    }

    @Bean
    public Binding commentCreatedBinding() {
        return BindingBuilder
                .bind(commentCreatedQueue())
                .to(commentEventsExchange())
                .with(commentCreatedRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
