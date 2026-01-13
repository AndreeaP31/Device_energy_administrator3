package com.example.demo.services;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.LocalUser;
import com.example.demo.repositories.LocalUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class UserSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncConsumer.class);
    private final LocalUserRepository localUserRepository;
    private final DeviceService deviceService;

    public UserSyncConsumer(LocalUserRepository localUserRepository, DeviceService deviceService) {
        this.localUserRepository = localUserRepository;
        this.deviceService = deviceService;
    }

    @RabbitListener(queues = RabbitMQConfig.SYNC_QUEUE)
    public void consumeUserMessage(UserDTO userDTO, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        LOGGER.info("Received event '{}' for user ID: {}", routingKey, userDTO.getId());

        try {
            if (routingKey.contains("created") || routingKey.contains("updated")) {

                LocalUser localUser = new LocalUser(userDTO.getId());
                localUserRepository.save(localUser);
                LOGGER.info("User synced (ID only): {}", userDTO.getId());

            } else if (routingKey.contains("deleted")) {

                LOGGER.info("Processing DELETE for user: {}", userDTO.getId());
                deviceService.deleteRelationsForUser(userDTO.getId());
                if (localUserRepository.existsById(userDTO.getId())) {
                    localUserRepository.deleteById(userDTO.getId());
                    LOGGER.info("User deleted from local sync: {}", userDTO.getId());
                } else {
                    LOGGER.warn("Received delete event for unknown user: {}", userDTO.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing user sync message", e);
        }
    }
}