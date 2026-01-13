package com.example.demo.services;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceUserRelationDTO;
import com.example.demo.dtos.MeasurementDTO;
import com.example.demo.entities.Measurement;
import com.example.demo.entities.MonitoredDevice;
import com.example.demo.repositories.MeasurementRepository;
import com.example.demo.repositories.MonitoredDeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class MonitoringConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringConsumer.class);

    private final MonitoredDeviceRepository deviceRepository;
    private final MeasurementRepository measurementRepository;
    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    public MonitoringConsumer(MonitoredDeviceRepository deviceRepository,
                              MeasurementRepository measurementRepository,
                              MonitoringService monitoringService,
                              ObjectMapper objectMapper) {
        this.deviceRepository = deviceRepository;
        this.measurementRepository = measurementRepository;
        this.monitoringService = monitoringService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.MONITORING_QUEUE)
    public void consumeMessage(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        byte[] body = message.getBody();

        try {
            if (routingKey.startsWith("device.created") || routingKey.startsWith("device.updated")) {
                DeviceDTO dto = objectMapper.readValue(body, DeviceDTO.class);
                handleDeviceUpsert(dto);

            } else if (routingKey.startsWith("device.assigned")) {
                DeviceUserRelationDTO dto = objectMapper.readValue(body, DeviceUserRelationDTO.class);
                handleDeviceAssign(dto);

            } else if (routingKey.startsWith("device.deleted")) {
                UUID deviceId = objectMapper.readValue(body, UUID.class);
                handleDeviceDelete(deviceId);

            } else if (routingKey.startsWith("sensor.measurement")) {
                MeasurementDTO dto = objectMapper.readValue(body, MeasurementDTO.class);
                handleSensorData(dto);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to process message {}: {}", routingKey, e.getMessage());
        }
    }

    private void handleDeviceUpsert(DeviceDTO dto) {
        MonitoredDevice device = deviceRepository.findById(dto.getId())
                .orElse(new MonitoredDevice(dto.getId(), dto.getMaxCons(),null));

        device.setMaxHourlyConsumption(dto.getMaxCons());


        deviceRepository.save(device);
        LOGGER.info("Synced monitored device: {}", dto.getId());
    }

    private void handleDeviceAssign(DeviceUserRelationDTO dto) {

        MonitoredDevice device = deviceRepository.findById(dto.getDeviceId())
                .orElseGet(() -> {
                    LOGGER.warn("Device {} not found during assignment. Creating placeholder.", dto.getDeviceId());
                    return new MonitoredDevice(dto.getDeviceId(), 0.0, null);
                });

        device.setUserId(dto.getUserId());
        deviceRepository.save(device);
        LOGGER.info("Successfully assigned user {} to device {} in Monitoring DB", dto.getUserId(), dto.getDeviceId());
    }

    private void handleDeviceDelete(UUID deviceId) {
        if (deviceRepository.existsById(deviceId)) {
            deviceRepository.deleteById(deviceId);
            LOGGER.info("Deleted monitored device: {}", deviceId);
        }
    }

    private void handleSensorData(MeasurementDTO dto) {

        if (!deviceRepository.existsById(dto.getDeviceId())) {

            LOGGER.warn("Received data for unknown device {}. Creating placeholder.", dto.getDeviceId());
            MonitoredDevice placeholder = new MonitoredDevice(dto.getDeviceId(), 10.0, null);
            deviceRepository.save(placeholder);
        }

        Measurement measurement = new Measurement(
                dto.getTimestamp(),
                dto.getDeviceId(),
                dto.getMeasurementValue()
        );
        measurementRepository.save(measurement);

        monitoringService.processMeasurement(dto);
    }
}