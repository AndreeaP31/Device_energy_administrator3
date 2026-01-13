package com.example.demo.dtos;

import java.util.UUID;

public class TokenValidationResponseDTO {

    private boolean valid;
    private UUID userId;
    private String role;
    private String message;

    public TokenValidationResponseDTO() {
    }

    public TokenValidationResponseDTO(boolean valid, UUID userId, String role, String message) {
        this.valid = valid;
        this.userId = userId;
        this.role = role;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
