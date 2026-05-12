package com.banking.accountservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "banking.exchange";
    public static final String CUSTOMER_CREATED_QUEUE = "customer.created.queue";
    public static final String ACCOUNT_CREATED_QUEUE = "account.created.queue";
    public static final String CUSTOMER_ROUTING_KEY = "customer.created";
    public static final String ACCOUNT_ROUTING_KEY = "account.created";

    @Bean
    public TopicExchange bankingExchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue customerCreatedQueue(){
        return QueueBuilder.durable(CUSTOMER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue accountCreatedQueue(){
        return QueueBuilder.durable(ACCOUNT_CREATED_QUEUE).build();
    }

    @Bean
    public Binding customerCreatedBinding(){
        return BindingBuilder
                .bind(customerCreatedQueue())
                .to(bankingExchange())
                .with(CUSTOMER_ROUTING_KEY);
    }

    @Bean
    public Binding accountCreatedBinding(){
        return BindingBuilder
                .bind(accountCreatedQueue())
                .to(bankingExchange())
                .with(ACCOUNT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

}
