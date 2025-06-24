package com.david.timeline_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.tweet-events}")
    private String tweetEventsExchange;
    @Value("${app.rabbitmq.exchange.follow-events}")
    private String followEventsExchange;

    @Value("${app.rabbitmq.queue.tweet-created}")
    private String tweetCreatedQueue;
    @Value("${app.rabbitmq.routing-key.tweet-created}")
    private String tweetCreatedRoutingKey;
    @Value("${app.rabbitmq.queue.unfollowed}")
    private String unfollowedQueue;

    @Value("${app.rabbitmq.queue.followed}")
    private String followedQueue;
    @Value("${app.rabbitmq.routing-key.followed}")
    private String followedRoutingKey;
    @Value("${app.rabbitmq.routing-key.unfollowed}")
    private String unfollowedRoutingKey;

    // Exchanges
    @Bean
    public TopicExchange tweetExchange() {
        return ExchangeBuilder
                .topicExchange(tweetEventsExchange)
                .durable(true).build();
    }

    @Bean
    public TopicExchange followExchange() {
        return ExchangeBuilder
                .topicExchange(followEventsExchange)
                .durable(true).build();
    }

    // Queues
    @Bean
    public Queue tweetCreatedQueue() {
        return QueueBuilder
                .durable(tweetCreatedQueue)
                .build();
    }

    @Bean
    public Queue followedQueue() {
        return QueueBuilder
                .durable(followedQueue)
                .build();
    }

    @Bean
    public Queue unfollowedQueue() {
        return QueueBuilder
                .durable(unfollowedQueue)
                .build();
    }

    // Bindings
    @Bean
    public Binding tweetCreatedBinding() {
        return BindingBuilder.bind(tweetCreatedQueue())
                .to(tweetExchange())
                .with(tweetCreatedRoutingKey);
    }

    @Bean
    public Binding followedBinding() {
        return BindingBuilder.bind(followedQueue())
                .to(followExchange())
                .with(followedRoutingKey);
    }

    @Bean
    public Binding unfollowedBinding() {
        return BindingBuilder.bind(unfollowedQueue())
                .to(followExchange())
                .with(unfollowedRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
