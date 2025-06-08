package com.david.tweet_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.tweet-events}")
    private String tweetEventsExchange;

    @Value("${app.rabbitmq.queue.tweet-created}")
    private String tweetCreatedQueue;
    @Value("${app.rabbitmq.queue.tweet-deleted}")
    private String tweetDeletedQueue;
    @Value("${app.rabbitmq.queue.tweet-liked}")
    private String tweetLikedQueue;

    @Value("${app.rabbitmq.routing-key.tweet-created}")
    private String tweetCreatedRoutingKey;
    @Value("${app.rabbitmq.routing-key.tweet-deleted}")
    private String tweetDeletedRoutingKey;
    @Value("${app.rabbitmq.routing-key.tweet-liked}")
    private String tweetLikedRoutingKey;

    @Bean
    public TopicExchange tweetEventsExchange() {
        return ExchangeBuilder.topicExchange(tweetEventsExchange)
                .durable(true)
                .build();
    }

    // Tweet created
    @Bean
    public Queue tweetCreatedQueue() {
        return QueueBuilder.durable(tweetCreatedQueue)
                .build();
    }

    @Bean
    public Binding tweetCreatedBinding() {
        return BindingBuilder.bind(tweetCreatedQueue())
                .to(tweetEventsExchange())
                .with(tweetCreatedRoutingKey);
    }

    // Tweet deleted
    @Bean
    public Queue tweetDeletedQueue() {
        return QueueBuilder.durable(tweetDeletedQueue)
                .build();
    }

    @Bean
    public Binding tweetDeletedBinding() {
        return BindingBuilder.bind(tweetDeletedQueue())
                .to(tweetEventsExchange())
                .with(tweetDeletedRoutingKey);
    }

    // Tweet liked
    @Bean
    public Queue tweetLikedQueue() {
        return QueueBuilder.durable(tweetLikedQueue)
                .build();
    }

    @Bean
    public Binding tweetLikedBinding() {
        return BindingBuilder.bind(tweetLikedQueue())
                .to(tweetEventsExchange())
                .with(tweetLikedRoutingKey);
    }
}
