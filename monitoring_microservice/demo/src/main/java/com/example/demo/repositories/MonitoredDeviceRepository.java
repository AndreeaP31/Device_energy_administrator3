package com.example.demo.repositories;

import com.example.demo.entities.MonitoredDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoredDeviceRepository extends JpaRepository<MonitoredDevice, UUID> {

    List<MonitoredDevice> findByUserId(UUID userId);
}