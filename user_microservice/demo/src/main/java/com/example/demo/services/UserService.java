package com.example.demo.services;


import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.dtos.builders.UserBuilder;
import com.example.demo.entities.User;
import com.example.demo.handlers.exceptions.model.BadCredentialsException;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth.service.url}")
    private String authServiceUrl;
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<UserDTO> findUsers() {
        List<User> UserList = userRepository.findAll();
        return UserList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        Optional<User> prosumerOptional = userRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return UserBuilder.toUserDetailsDTO(prosumerOptional.get());
    }
//    @Transactional
//    public UserDetailsDTO updateUser(UUID id, UserDetailsDTO userDetailsDTO) {
//        User userToUpdate = userRepository.findById(id)
//                .orElseThrow(() -> {
//                    LOGGER.error("Update failed: User with ID {} not found.", id);
//                    return new ResourceNotFoundException("User with id: " + id + " not found.");
//                });
//
//        userToUpdate.setName(userDetailsDTO.getName());
//
//        User updatedUser = userRepository.save(userToUpdate);
//        LOGGER.debug("Successfully updated user with ID {}.", updatedUser.getId());
//
//        return UserBuilder.toUserDetailsDTO(updatedUser);
//    }
    @Transactional
    public UserDetailsDTO updateUser(UUID id, UserDetailsDTO userDetailsDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setName(userDetailsDTO.getName());
        User updatedUser = userRepository.save(user);

        try {
            UserDTO syncData = new UserDTO(updatedUser.getId(), updatedUser.getName());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.updated", syncData);
        } catch (Exception e) { LOGGER.error("Failed to send update event", e); }

        return UserBuilder.toUserDetailsDTO(updatedUser);
    }

    public UUID insert(UserDetailsDTO userDetails) {
        User user = new User(userDetails.getName());
        if (userDetails.getId() != null) {
            user.setId(userDetails.getId());
        } else {
            user.setId(UUID.randomUUID());
        }

        user = userRepository.save(user);
        LOGGER.debug("User inserted: {}", user.getId());

        try {
            UserDTO syncData = new UserDTO(user.getId(), user.getName());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.created", syncData);
            LOGGER.info("Sent user.created event for {}", user.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to send user.created event", e);
        }

        return user.getId();
    }
//    public UUID insert(UserDetailsDTO UserDTO) {
//        User user = UserBuilder.toEntity(UserDTO);
//        user = userRepository.save(user);
//        LOGGER.debug("User with id {} was inserted in db", user.getId());
//        return user.getId();
//    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }

        try {
            restTemplate.delete(authServiceUrl + "/auth/internal/usercred/" + userId);
        } catch (Exception e) {
            LOGGER.error("Auth service delete error", e);
        }


        userRepository.deleteById(userId);


        try {
            // Putem trimite un DTO simplu sau doar ID-ul într-un obiect
            UserDTO syncData = new UserDTO();
            syncData.setId(userId);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.deleted", syncData);
            LOGGER.info("Sent user.deleted event for {}", userId);
        } catch (Exception e) {
            LOGGER.error("Failed to send delete event", e);
        }
    }
//    @Transactional
//    public void deleteUser(UUID userId) {
//        if (!userRepository.existsById(userId)) {
//            throw new ResourceNotFoundException("User with id: " + userId + " not found.");
//        }
//
//        try {
//            restTemplate.delete(authServiceUrl + "/auth/internal/usercred/" + userId);
//            LOGGER.debug("Credentials deleted for user {}", userId);
//        } catch (Exception e) {
//            LOGGER.error("Could not delete credentials for user {}", userId, e);
//            throw new IllegalStateException("Auth-Service unavailable, cannot delete user.");
//        }
//
//        try {
//            restTemplate.delete(deviceServiceUrl + "/device/internal/relations/" + userId);
//            LOGGER.debug("Device relations deleted for user {}", userId);
//        } catch (Exception e) {
//            LOGGER.error("Could not delete device relations for user {}", userId, e);
//            throw new IllegalStateException("Device-Service unavailable, cannot delete user.");
//        }
//
//        userRepository.deleteById(userId);
//        LOGGER.debug("Successfully deleted user {}", userId);
//    }



}
