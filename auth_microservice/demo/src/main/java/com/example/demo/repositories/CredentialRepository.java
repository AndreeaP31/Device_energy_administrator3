package com.example.demo.repositories;

import com.example.demo.entities.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Optional<Credential> findByUsername(String username);

    Optional<Credential> findByUserId(UUID userId);

    boolean existsByUsername(String username);

    void deleteByUserId(UUID userId);
}
