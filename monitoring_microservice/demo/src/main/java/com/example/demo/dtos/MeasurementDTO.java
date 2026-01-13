package com.example.demo.dtos;

import java.util.UUID;

public class MeasurementDTO {
    private long timestamp;
    private UUID deviceId;
    private double measurementValue;

    public MeasurementDTO() {}

    public MeasurementDTO(long timestamp, UUID deviceId, double measurementValue) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public double getMeasurementValue() { return measurementValue; }
    public void setMeasurementValue(double measurementValue) { this.measurementValue = measurementValue; }

    @Override
    public String toString() {
        return "Measurement{device=" + deviceId + ", val=" + measurementValue + "}";
    }
}