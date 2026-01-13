package com.example.demo.dtos.builders;

import com.example.demo.dtos.AuthResponseDTO;
import com.example.demo.entities.Credential;

public class CredentialBuilder {

    private CredentialBuilder() {
    }

    public static AuthResponseDTO toAuthResponseDTO(String token, Credential credential) {
        return new AuthResponseDTO(
                token,
                credential.getUserId(),
                credential.getUsername(),
                credential.getRole()
        );
    }
}
