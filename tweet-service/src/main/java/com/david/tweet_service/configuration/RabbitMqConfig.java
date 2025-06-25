package com.david.tweet_service.configuration;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.tweet-events}")
    private String tweetEventsExchange;

    @Bean
    public TopicExchange tweetEventsExchange() {
        return ExchangeBuilder.topicExchange(tweetEventsExchange)
                .durable(true)
                .build();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, TopicExchange tweetEventsExchange) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(tweetEventsExchange.getName());
        template.setMessageConverter(messageConverter());
        return template;

    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
