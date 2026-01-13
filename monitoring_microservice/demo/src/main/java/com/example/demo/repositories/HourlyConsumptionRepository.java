package com.example.demo.repositories;

import com.example.demo.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {

    Optional<HourlyConsumption> findByDeviceIdAndTimestamp(UUID deviceId, long timestamp);
    List<HourlyConsumption> findByDeviceIdOrderByTimestampAsc(UUID deviceId);
    List<HourlyConsumption> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(UUID deviceId, long start, long end);
}