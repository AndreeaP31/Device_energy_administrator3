package com.example.demo.dtos;


import java.util.UUID;

public class AlertMessage {
    private String userId;
    private String deviceId;
    private String description;

    public AlertMessage(String deviceId, String userId, String description) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCurrentConsumption() {
        return currentConsumption;
    }

    public void setCurrentConsumption(double currentConsumption) {
        this.currentConsumption = currentConsumption;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    private double currentConsumption;
    private double limit;
}