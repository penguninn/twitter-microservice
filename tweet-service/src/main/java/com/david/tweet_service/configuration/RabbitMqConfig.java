package com.david.tweet_service.configuration;

import jakarta.validation.Valid;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
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
}
