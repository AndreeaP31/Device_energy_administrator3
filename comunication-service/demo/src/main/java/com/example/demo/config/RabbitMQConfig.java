package com.example.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// comunication-service/src/main/java/com/example/demo/config/RabbitMQConfig.java
@Configuration
public class RabbitMQConfig {
    public static final String OVERCONSUMPTION_QUEUE = "overconsumption_queue";

    @Bean
    public Queue overconsumptionQueue() {
        return new Queue(OVERCONSUMPTION_QUEUE, true); // Aceasta va crea coada în RabbitMQ
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
