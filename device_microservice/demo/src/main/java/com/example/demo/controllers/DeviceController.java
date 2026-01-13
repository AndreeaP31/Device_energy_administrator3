package com.example.demo.controllers;

import com.example.demo.dtos.*;
import com.example.demo.entities.DeviceUserRelation;
import com.example.demo.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/device")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @PostMapping("/assign")
    public ResponseEntity<UUID> assignUserToDevice(@Valid @RequestBody DeviceUserRelationDTO relationDto) {
        UUID relationId = deviceService.assignUserToDevice(relationDto);
        return ResponseEntity.ok(relationId);
    }


    @GetMapping("/{userId}/for-user/devices")
    public List<DeviceDTO> findDevicesForUser(@PathVariable UUID userId) {
        return deviceService.findDevicesByUserId(userId);
    }

    @GetMapping("/{userId}/devices")
    public List<DeviceUserRelation> getDevicesForUser(@PathVariable UUID userId) {
        return deviceService.getDevicesForUser(userId);
    }
    @GetMapping("/with-users")
    public List<DeviceWithUsersDTO> getDevicesWithUsers() {
        return deviceService.getDevicesWithUsers();
    }


    @GetMapping("/{deviceId}/users")
    public List<DeviceUserRelation> getUsersForDevice(@PathVariable UUID deviceId) {
        return deviceService.getUsersForDevice(deviceId);
    }

    @DeleteMapping("/{deviceId}/unassign")
    public void unassign(@PathVariable UUID deviceId) {
        deviceService.unassignDevice(deviceId);
    }


    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> updateDevice(@PathVariable UUID id, @Valid @RequestBody DeviceDetailsDTO deviceDetailsDTO) {
        DeviceDetailsDTO updatedDevice = deviceService.updateDevice(id, deviceDetailsDTO);
        return ResponseEntity.ok(updatedDevice);
    }
    @GetMapping("/relations")
    public ResponseEntity<List<RelationDTO>> getAllRelations() {
        return ResponseEntity.ok(deviceService.getAllRelationsWithNames());
    }


    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody DeviceDetailsDTO Device) {
        UUID id = deviceService.insert(Device);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build(); // 201 + Location header
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/internal/relations/{userId}")
    public ResponseEntity<Void> deleteRelationsForUser(@PathVariable UUID userId) {
        deviceService.deleteRelationsForUser(userId);
        return ResponseEntity.noContent().build();
    }


}
