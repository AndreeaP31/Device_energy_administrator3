package com.example.demo.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private int maxCons;

    public DeviceDTO() {}
    public DeviceDTO(UUID id, String name, int maxCons) {
        this.id = id; this.name = name; this.maxCons = maxCons;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxCons() { return maxCons; }
    public void setMaxCons(int maxCons) { this.maxCons = maxCons; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDTO that = (DeviceDTO) o;
        return maxCons == that.maxCons && Objects.equals(name, that.name);
    }
    @Override public int hashCode() { return Objects.hash(name, maxCons); }
}
