package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    private String userId;
    private String deviceId;
    private String description;
    private double currentConsumption;
    private double limit;
}