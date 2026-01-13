package com.example.demo.dtos;

import com.example.demo.entities.Credential;

import java.util.UUID;

public class CredentialInfoDTO {

    private UUID userId;
    private String username;
    private Credential.Role role;

    public CredentialInfoDTO() {}

    public CredentialInfoDTO(UUID userId, String username, Credential.Role role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
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
