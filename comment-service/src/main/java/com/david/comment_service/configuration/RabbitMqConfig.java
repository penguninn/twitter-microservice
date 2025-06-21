package com.david.comment_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange.comment-events}")
    private String commentEventsExchange;


    @Bean
    public TopicExchange commentEventsExchange() {
        return ExchangeBuilder.topicExchange(commentEventsExchange)
                .durable(true)
                .build();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, TopicExchange commentEventsExchange) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(commentEventsExchange.getName());
        return template;
    }


}
