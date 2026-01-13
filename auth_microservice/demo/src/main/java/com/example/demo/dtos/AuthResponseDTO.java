package com.example.demo.dtos;

import com.example.demo.entities.Credential;

import java.util.UUID;

public class AuthResponseDTO {

    private String token;
    private UUID userId;
    private String username;
    private Credential.Role role;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, UUID userId, String username, Credential.Role role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Credential.Role getRole() {
        return role;
    }

    public void setRole(Credential.Role role) {
        this.role = role;
    }
}
