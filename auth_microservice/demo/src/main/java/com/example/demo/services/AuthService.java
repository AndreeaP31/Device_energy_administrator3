package com.example.demo.services;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.*;
import com.example.demo.entities.Credential;
import com.example.demo.repositories.CredentialRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final RabbitTemplate rabbitTemplate;


    public AuthService(
            CredentialRepository credentialRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RabbitTemplate rabbitTemplate) {

        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (credentialRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        UUID userId = UUID.randomUUID();

        Credential credential = new Credential(
                userId,
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getRoleAsEnum()
        );
        credentialRepository.save(credential);

        UserDTO userPayload = new UserDTO(userId, request.getName());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_REGISTER,
                    userPayload
            );
            LOGGER.info("Sent auth.register event for user {}", userId);
        } catch (Exception e) {
            LOGGER.error("Failed to send RabbitMQ message", e);
        }

        String token = jwtService.generateToken(
                credential.getUserId(),
                credential.getUsername(),
                credential.getRole()
        );

        return new AuthResponseDTO(
                token,
                credential.getUserId(),
                credential.getUsername(),
                credential.getRole()
        );
    }

    public List<CredentialInfoDTO> getAllCredentials() {
        return credentialRepository.findAll()
                .stream()
                .map(cred -> new CredentialInfoDTO(
                        cred.getUserId(),
                        cred.getUsername(),
                        cred.getRole()
                ))
                .toList();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        Optional<Credential> credentialOpt = credentialRepository.findByUsername(request.getUsername());
        if (credentialOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), credentialOpt.get().getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        Credential credential = credentialOpt.get();
        String token = jwtService.generateToken(credential.getUserId(), credential.getUsername(), credential.getRole());
        return new AuthResponseDTO(token, credential.getUserId(), credential.getUsername(), credential.getRole());
    }
//    public AuthResponseDTO login(LoginRequestDTO request) {
//
//        Optional<Credential> credentialOpt =
//                credentialRepository.findByUsername(request.getUsername());
//
//        if (credentialOpt.isEmpty()) {
//            throw new IllegalArgumentException("Invalid username or password");
//        }
//
//        Credential credential = credentialOpt.get();
//
//        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
//            throw new IllegalArgumentException("Invalid username or password");
//        }
//
//        String token = jwtService.generateToken(
//                credential.getUserId(),
//                credential.getUsername(),
//                credential.getRole()
//        );
//
//        return new AuthResponseDTO(
//                token,
//                credential.getUserId(),
//                credential.getUsername(),
//                credential.getRole()
//        );
//    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public UUID extractUserId(String token) {
        return jwtService.extractUserId(token);
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    public String extractRole(String token) {
        return jwtService.extractRole(token);
    }

    @Transactional
    public void deleteCredentialByUserId(UUID userId) {
        credentialRepository.deleteByUserId(userId);
        LOGGER.info("Credentials removed for user {}", userId);
    }
}
