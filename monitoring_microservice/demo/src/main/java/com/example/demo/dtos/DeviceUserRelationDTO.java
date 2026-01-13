package com.example.demo.dtos;

import java.util.UUID;

public class DeviceUserRelationDTO {
    private UUID userId;
    private UUID deviceId;

    public DeviceUserRelationDTO() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
}