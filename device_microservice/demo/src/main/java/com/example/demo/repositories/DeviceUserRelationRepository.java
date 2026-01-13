package com.example.demo.repositories;

import com.example.demo.entities.DeviceUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface DeviceUserRelationRepository extends JpaRepository<DeviceUserRelation, UUID> {

    List<DeviceUserRelation> findByUserId(UUID userId);

    List<DeviceUserRelation> findByDeviceId(UUID deviceId);
    @Transactional
    void deleteByDeviceId(UUID deviceId);

    @Transactional
    void deleteByUserId(UUID userId);
}

