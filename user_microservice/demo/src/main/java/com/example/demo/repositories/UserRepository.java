package com.example.demo.repositories;

import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<User> findByName(String name);

    Optional<User> findById(UUID name);
    /**
     * Example: Custom query
     */

    Optional<User> findSeniorsByName(@Param("name") String name);

}
