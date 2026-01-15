package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "energy-platform-exchange";
    public static final String MONITORING_QUEUE = "monitoring-queue";
    public static final String OVERCONSUMPTION_QUEUE = "overconsumption_queue";

    @Bean
    public Queue overconsumptionQueue() {
        return new Queue(OVERCONSUMPTION_QUEUE, true);
    }
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        return new Queue(MONITORING_QUEUE, true);
    }
//shd
    @Bean
    public Binding bindingDevice(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("device.#");
    }
//s
    @Bean
    public Binding bindingSensor(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("sensor.measurement");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}