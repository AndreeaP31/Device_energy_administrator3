package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "monitored_devices")
public class MonitoredDevice {

    @Id
    private UUID id;

    private Double maxHourlyConsumption;

    private UUID userId;
    public MonitoredDevice() {}

    public MonitoredDevice(UUID id, Double maxHourlyConsumption, UUID userId) {
        this.id = id;
        this.maxHourlyConsumption = maxHourlyConsumption;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Double getMaxHourlyConsumption() { return maxHourlyConsumption; }
    public void setMaxHourlyConsumption(Double maxHourlyConsumption) { this.maxHourlyConsumption = maxHourlyConsumption; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}