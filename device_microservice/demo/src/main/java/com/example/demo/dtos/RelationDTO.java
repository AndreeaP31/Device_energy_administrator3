package com.example.demo.dtos;

import java.util.UUID;

public class RelationDTO {

    private UUID id;
    private UUID userId;
    private UUID deviceId;
    private String userName; // ← ADĂUGAT
    private String deviceName;

    public RelationDTO() {}

    public RelationDTO(UUID id, UUID userId, UUID deviceId, String userName, String deviceName) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userName = userName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

}
