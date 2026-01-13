package com.example.demo.dtos;

import java.util.List;
import java.util.UUID;

public class DeviceWithUsersDTO {
    private UUID id;
    private String name;
    private int maxCons;
    private List<String> users;

    public DeviceWithUsersDTO(UUID id, String name, int maxCons, List<String> users) {
        this.id = id;
        this.name = name;
        this.maxCons = maxCons;
        this.users = users;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getMaxCons() { return maxCons; }
    public List<String> getUsers() { return users; }
}
