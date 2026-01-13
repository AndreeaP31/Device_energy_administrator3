package com.example.demo.dtos.builders;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device Device) {
        return new DeviceDTO(Device.getId(), Device.getName(), Device.getMaxCons());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device Device) {
        return new DeviceDetailsDTO(Device.getId(), Device.getName(),  Device.getMaxCons());
    }

    public static Device toEntity(DeviceDetailsDTO deviceDetailsDTO) {
        return new Device(deviceDetailsDTO.getName(),
                deviceDetailsDTO.getMaxCons());
    }
}
