package com.example.demo.controllers;

import com.example.demo.dtos.AuthResponseDTO;
import com.example.demo.dtos.CredentialInfoDTO;
import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping("/internal/credentials")
    public ResponseEntity<List<CredentialInfoDTO>> getAllCredentials() {
        return ResponseEntity.ok(authService.getAllCredentials());
    }

    @DeleteMapping("/internal/usercred/{userId}")
    public ResponseEntity<Void> deleteCredentials(@PathVariable UUID userId) {
        authService.deleteCredentialByUserId(userId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        AuthResponseDTO response = authService.login(request);


        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("\nVALIDATE CALL:");
        System.out.println("   AuthHeader=" + authHeader);

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "Invalid token format");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);

        System.out.println("   Extracted token=" + token);

        boolean valid = authService.validateToken(token);
        System.out.println("   Token valid=" + valid);

        response.put("valid", valid);

        if (valid) {
            UUID userId = authService.extractUserId(token);
            String username = authService.extractUsername(token);
            String role = authService.extractRole(token);

            System.out.println("   Payload:");
            System.out.println("      userId=" + userId);
            System.out.println("      username=" + username);
            System.out.println("      role=" + role);

            response.put("userId", userId);
            response.put("username", username);
            response.put("role", role);
        }


        return ResponseEntity.ok(response);
    }

}
