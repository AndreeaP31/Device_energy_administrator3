package com.example.demo.dtos;


import com.example.demo.dtos.validators.annotation.AgeLimit;
import com.example.demo.dtos.validators.annotation.AgeLimit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;
    @NotNull(message = "maxCons is required")
    private Integer maxCons;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(String name, int maxCons) {
        this.name = name;
        this.maxCons = maxCons;
    }

    public DeviceDetailsDTO(UUID id, String name, int maxCons) {
        this.id = id;
        this.name = name;
        this.maxCons = maxCons;
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


    public int getMaxCons() {
        return maxCons;
    }

    public void setMaxCons(int maxCons) {
        this.maxCons = maxCons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return maxCons == that.maxCons &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, maxCons);
    }
}
