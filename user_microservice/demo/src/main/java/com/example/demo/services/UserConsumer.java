package com.example.demo.services;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConsumer.class);
    private final UserService userService;

    public UserConsumer(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeRegisterEvent(UserDTO userDTO) {
        LOGGER.info("Received auth.register event for user: {}", userDTO.getId());

        try {
            UserDetailsDTO details = new UserDetailsDTO(userDTO.getId(), userDTO.getName());
            userService.insert(details);

        } catch (Exception e) {
            LOGGER.error("Failed to process user creation from event", e);
        }
    }
}