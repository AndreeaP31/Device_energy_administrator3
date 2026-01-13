package com.example.demo.dtos;

import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Double maxCons;

    public DeviceDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getMaxCons() { return maxCons; }
    public void setMaxCons(Double maxCons) { this.maxCons = maxCons; }

    @Override
    public String toString() {
        return "DeviceDTO{id=" + id + ", maxCons=" + maxCons + "}";
    }
}