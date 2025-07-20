package com.david.search_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    
    // Exchange values
    @Value("${app.rabbitmq.exchange.identity-events}")
    private String identityEventsExchange;
    
    @Value("${app.rabbitmq.exchange.profile-events}")
    private String profileEventsExchange;
    
    @Value("${app.rabbitmq.exchange.tweet-events}")
    private String tweetEventsExchange;
    
    // Queue values
    @Value("${app.rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;
    
    @Value("${app.rabbitmq.queue.search-tweet-created}")
    private String searchTweetCreatedQueue;
    
    @Value("${app.rabbitmq.queue.search-tweet-updated}")
    private String searchTweetUpdatedQueue;
    
    @Value("${app.rabbitmq.queue.search-tweet-deleted}")
    private String searchTweetDeletedQueue;
    
    @Value("${app.rabbitmq.queue.search-profile-updated}")
    private String searchProfileUpdatedQueue;
    
    // Routing key values
    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;
    
    @Value("${app.rabbitmq.routing-key.tweet-created}")
    private String tweetCreatedRoutingKey;
    
    @Value("${app.rabbitmq.routing-key.tweet-updated}")
    private String tweetUpdatedRoutingKey;
    
    @Value("${app.rabbitmq.routing-key.tweet-deleted}")
    private String tweetDeletedRoutingKey;
    
    @Value("${app.rabbitmq.routing-key.profile-updated}")
    private String profileUpdatedRoutingKey;

    @Bean
    public TopicExchange identityEventsExchange() {
        return ExchangeBuilder.topicExchange(identityEventsExchange)
                .durable(true)
                .build();
    }
    
    @Bean
    public TopicExchange profileEventsExchange() {
        return ExchangeBuilder.topicExchange(profileEventsExchange)
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
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(userRegisteredQueue)
                .build();
    }
    
    @Bean
    public Queue searchTweetCreatedQueue() {
        return QueueBuilder.durable(searchTweetCreatedQueue)
                .build();
    }
    
    @Bean
    public Queue searchTweetUpdatedQueue() {
        return QueueBuilder.durable(searchTweetUpdatedQueue)
                .build();
    }
    
    @Bean
    public Queue searchTweetDeletedQueue() {
        return QueueBuilder.durable(searchTweetDeletedQueue)
                .build();
    }
    
    @Bean
    public Queue searchProfileUpdatedQueue() {
        return QueueBuilder.durable(searchProfileUpdatedQueue)
                .build();
    }


    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(identityEventsExchange())
                .with(userRegisteredRoutingKey);
    }
    
    @Bean
    public Binding searchTweetCreatedBinding() {
        return BindingBuilder.bind(searchTweetCreatedQueue())
                .to(tweetEventsExchange())
                .with(tweetCreatedRoutingKey);
    }
    
    @Bean
    public Binding searchTweetUpdatedBinding() {
        return BindingBuilder.bind(searchTweetUpdatedQueue())
                .to(tweetEventsExchange())
                .with(tweetUpdatedRoutingKey);
    }
    
    @Bean
    public Binding searchTweetDeletedBinding() {
        return BindingBuilder.bind(searchTweetDeletedQueue())
                .to(tweetEventsExchange())
                .with(tweetDeletedRoutingKey);
    }
    
    @Bean
    public Binding searchProfileUpdatedBinding() {
        return BindingBuilder.bind(searchProfileUpdatedQueue())
                .to(profileEventsExchange())
                .with(profileUpdatedRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}