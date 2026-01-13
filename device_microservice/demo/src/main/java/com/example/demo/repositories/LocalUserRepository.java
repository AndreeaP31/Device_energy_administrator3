package com.example.demo.repositories;
import com.example.demo.entities.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LocalUserRepository extends JpaRepository<LocalUser, UUID> {
}
