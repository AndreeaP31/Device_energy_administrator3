package com.example.demo.services;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.UserDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthConsumer {

    private final AuthService authService;

    public AuthConsumer(AuthService authService) {
        this.authService = authService;
    }

    @RabbitListener(queues = RabbitMQConfig.CLEANUP_QUEUE)
    public void consumeUserDeletion(UserDTO userDTO) {
        System.out.println("Received delete event for user: " + userDTO.getId());
        authService.deleteCredentialByUserId(userDTO.getId());
    }
}