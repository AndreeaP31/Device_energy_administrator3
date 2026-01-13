package com.example.demo.services;
import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dtos.MeasurementDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@Service
public class SimulatorService implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    @Value("${simulator.device-id}")
    private String deviceIdString;


    public SimulatorService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        UUID deviceId;
        try {
            deviceId = UUID.fromString(deviceIdString);
        } catch (IllegalArgumentException e) {
            System.err.println(" INVALID DEVICE ID. Set correct UUID in application.properties or Docker.");
            return;
        }

        System.out.println("Smart Meter Simulator STARTED for Device: " + deviceId);

        double currentLoad = 0.5 + random.nextDouble();

        long simulatedTime = System.currentTimeMillis();

        while (true) {
            LocalDateTime currentTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(simulatedTime), ZoneId.systemDefault());
            int hour = currentTime.getHour();

            double timeFactor = 1.0;
            if (hour >= 23 || hour < 7) {
                timeFactor = 0.3;

            } else if (hour >= 18 && hour < 23) {
                timeFactor = 1.5;
            }
            double noise = (random.nextDouble() - 0.5) * 0.2;
            double measurementValue = (currentLoad * timeFactor) + noise;

            if (measurementValue < 0) measurementValue = 0.05;

            MeasurementDTO measurement = new MeasurementDTO(
                    simulatedTime,
                    deviceId,
                    measurementValue
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "sensor.measurement",
                    measurement
            );

            System.out.println("Time: " + currentTime.toLocalTime() +
                    " | Val: " + String.format("%.2f", measurementValue) + " kWh");

            simulatedTime += 10 * 60 * 1000;

            TimeUnit.SECONDS.sleep(2);
        }
    }
}