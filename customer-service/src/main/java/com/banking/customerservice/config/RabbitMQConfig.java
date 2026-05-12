package com.banking.customerservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitMQConfig {

    public static final String EXCHANGE = "banking.exchange";
    public static final String CUSTOMER_CREATED_QUEUE = "customer.created.queue";
    public static final String CUSTOMER_ROUTING_KEY = "customer.created";

    // The exchange
    @Bean
    public TopicExchange bankingExchange(){
        return new TopicExchange(EXCHANGE);
    }


    // The queue
    @Bean
    public Queue customerCreatedQueue(){
        return QueueBuilder.durable(CUSTOMER_CREATED_QUEUE).build();
    }

    // Binds the queue to the exchange via the routing key
    @Bean
    public Binding customerCreatedBinding(){
        return BindingBuilder
                .bind(customerCreatedQueue())
                .to(bankingExchange())
                .with(CUSTOMER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }



}
