package com.example.demo.services;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.*;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.entities.DeviceUserRelation;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.DeviceUserRelationRepository;
import com.example.demo.repositories.LocalUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceUserRelationRepository relationRepository;
    private final LocalUserRepository localUserRepository;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         DeviceUserRelationRepository relationRepository,
                         LocalUserRepository localUserRepository,
                         RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.relationRepository = relationRepository;
        this.localUserRepository = localUserRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(deviceOptional.get());
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device inserted: {}", device.getId());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "device.created",
                    deviceDTO
            );
        } catch (Exception e) {
            LOGGER.error("Failed to send device.created event", e);
        }

        return device.getId();
    }

    public List<DeviceWithUsersDTO> getDevicesWithUsers() {
        List<Device> devices = deviceRepository.findAll();

        return devices.stream().map(device -> {
            List<DeviceUserRelation> relations = relationRepository.findByDeviceId(device.getId());
            List<UUID> userIds = relations.stream()
                    .map(DeviceUserRelation::getUserId)
                    .toList();

            List<String> userNames = userIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());

            return new DeviceWithUsersDTO(
                    device.getId(),
                    device.getName(),
                    device.getMaxCons(),
                    userNames
            );
        }).toList();
    }

    public List<RelationDTO> getAllRelationsWithNames() {
        List<DeviceUserRelation> relations = relationRepository.findAll();

        return relations.stream().map(relation -> {
            String userName = relation.getUserId().toString();

            String deviceName = "(unknown)";
            try {
                Optional<Device> deviceOpt = deviceRepository.findById(relation.getDeviceId());
                if (deviceOpt.isPresent()) {
                    deviceName = deviceOpt.get().getName();
                }
            } catch (Exception e) {
                deviceName = "(unknown)";
            }

            return new RelationDTO(
                    relation.getId(),
                    relation.getUserId(),
                    relation.getDeviceId(),
                    userName, // Aici va fi UUID-ul acum
                    deviceName
            );
        }).toList();
    }
    public UUID assignUserToDevice(DeviceUserRelationDTO dto) {

        if (!localUserRepository.existsById(dto.getUserId())) {
            throw new ResourceNotFoundException("User " + dto.getUserId() + " not found locally.");
        }
        if (!deviceRepository.existsById(dto.getDeviceId())) {
            throw new ResourceNotFoundException("Device " + dto.getDeviceId() + " not found.");
        }

        DeviceUserRelation relation = new DeviceUserRelation(dto.getUserId(), dto.getDeviceId());
        relation = relationRepository.save(relation);

        LOGGER.info("Device {} assigned to User {}", dto.getDeviceId(), dto.getUserId());

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.assigned", dto);
        } catch (Exception e) {
            LOGGER.error("Failed to send device.assigned event", e);
        }

        return relation.getId();
    }
    public List<DeviceDTO> findDevicesByUserId(UUID userId) {
        List<DeviceUserRelation> relations = relationRepository.findByUserId(userId);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> deviceIds = relations.stream()
                .map(DeviceUserRelation::getDeviceId)
                .collect(Collectors.toList());

        List<Device> devices = deviceRepository.findAllById(deviceIds);

        return devices.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceUserRelation> getDevicesForUser(UUID userId) {
        return relationRepository.findByUserId(userId);
    }

    public List<DeviceUserRelation> getUsersForDevice(UUID deviceId) {
        return relationRepository.findByDeviceId(deviceId);
    }

    public void unassignDevice(UUID deviceId) {
        relationRepository.deleteByDeviceId(deviceId);
    }

    @Transactional
    public void deleteDevice(UUID deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device not found");
        }

        relationRepository.deleteByDeviceId(deviceId);
        deviceRepository.deleteById(deviceId);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "device.deleted",
                    deviceId
            );
        } catch (Exception e) {
            LOGGER.error("Failed to send device.deleted event", e);
        }
    }

    public void deleteRelationsForUser(UUID userId) {
        relationRepository.deleteByUserId(userId);
        LOGGER.debug("Deleted all relations for user ID {}.", userId);
    }

    @Transactional
    public DeviceDetailsDTO updateDevice(UUID id, DeviceDetailsDTO deviceDetailsDTO) {
        Device deviceToUpdate = deviceRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Update failed: Device with ID {} not found.", id);
                    return new ResourceNotFoundException("Device with id: " + id + " not found.");
                });

        deviceToUpdate.setName(deviceDetailsDTO.getName());
        deviceToUpdate.setMaxCons(deviceDetailsDTO.getMaxCons());

        Device updatedDevice = deviceRepository.save(deviceToUpdate);

        // Op
        try {
            DeviceDetailsDTO eventDto = new DeviceDetailsDTO(updatedDevice.getId(), updatedDevice.getName(), updatedDevice.getMaxCons());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.updated", eventDto);
        } catch(Exception e) {
            LOGGER.error("Failed update event", e);
        }

        LOGGER.debug("Successfully updated device with ID {}.", updatedDevice.getId());
        return DeviceBuilder.toDeviceDetailsDTO(updatedDevice);
    }
}