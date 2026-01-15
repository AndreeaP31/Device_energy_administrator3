package com.example.demo.services;

import com.example.demo.dtos.AlertMessage;
import com.example.demo.dtos.MeasurementDTO;
import com.example.demo.entities.HourlyConsumption;
import com.example.demo.entities.MonitoredDevice;
import com.example.demo.repositories.HourlyConsumptionRepository;
import com.example.demo.repositories.MonitoredDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringService.class);

    private final HourlyConsumptionRepository consumptionRepository;
    private final MonitoredDeviceRepository deviceRepository;


    private final RabbitTemplate rabbitTemplate;

    // 2. Adaugă-l în constructor pentru Dependency Injection
    public MonitoringService(HourlyConsumptionRepository consumptionRepository,
                             MonitoredDeviceRepository deviceRepository,
                             RabbitTemplate rabbitTemplate) {
        this.consumptionRepository = consumptionRepository;
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void processMeasurement(MeasurementDTO measurementDTO) {
        Optional<MonitoredDevice> deviceOpt = deviceRepository.findById(measurementDTO.getDeviceId());

        if (deviceOpt.isEmpty()) {
            LOGGER.warn("Received data for unknown device ID: {}", measurementDTO.getDeviceId());
            return;
        }

        MonitoredDevice device = deviceOpt.get();

        long currentHourTimestamp = getHourTimestamp(measurementDTO.getTimestamp());

        HourlyConsumption consumption = consumptionRepository
                .findByDeviceIdAndTimestamp(device.getId(), currentHourTimestamp)
                .orElse(new HourlyConsumption(device.getId(), currentHourTimestamp, 0.0));

        double newTotal = consumption.getTotalConsumption() + measurementDTO.getMeasurementValue();
        consumption.setTotalConsumption(newTotal);

        consumptionRepository.save(consumption);
        LOGGER.info("Updated consumption for device {}: {} (+{})", device.getId(), newTotal, measurementDTO.getMeasurementValue());
        if(newTotal > device.getMaxHourlyConsumption()) {
            AlertMessage alert = new AlertMessage(
                    device.getUserId().toString(),
                    device.getId().toString(),
                    "Consumul de " + newTotal + " a depasit limita de " + device.getMaxHourlyConsumption()
            );
            rabbitTemplate.convertAndSend("overconsumption_queue", alert);
            LOGGER.info("Alertă trimisă pentru device-ul {}", device.getId());
        }

    }



    private long getHourTimestamp(long timestampInMillis) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampInMillis), ZoneId.systemDefault());
        LocalDateTime hourStart = dateTime.truncatedTo(ChronoUnit.HOURS);
        return hourStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public List<HourlyConsumption> getHourlyConsumption(UUID deviceId) {

        return consumptionRepository.findByDeviceIdOrderByTimestampAsc(deviceId);
    }


    public List<HourlyConsumption> getConsumptionForDay(UUID deviceId, long dateInMillis) {

        LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(dateInMillis), ZoneId.systemDefault());

        long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        long endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return consumptionRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(deviceId, startOfDay, endOfDay);
    }

}