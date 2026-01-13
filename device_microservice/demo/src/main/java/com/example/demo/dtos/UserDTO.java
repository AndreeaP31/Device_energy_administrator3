package com.example.demo.dtos;

import java.io.Serializable;
import java.util.UUID;

public class UserDTO implements Serializable {

    private UUID id;
    private String name;

    public UserDTO() {
    }

    public UserDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserDTO{id=" + id + ", name='" + name + "'}";
    }
}