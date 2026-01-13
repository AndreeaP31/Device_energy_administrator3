package com.example.demo.controllers;

//import com.example.demo.entities.HourlyConsumption;
//import com.example.demo.services.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {
//
//    private final MonitoringService monitoringService;
//
//    public MonitoringController(MonitoringService monitoringService) {
//        this.monitoringService = monitoringService;
//    }
//
//    @GetMapping("/{deviceId}")
//    public ResponseEntity<List<HourlyConsumption>> getConsumptionHistory(@PathVariable UUID deviceId) {
//        List<HourlyConsumption> history = monitoringService.getHourlyConsumption(deviceId);
//        return ResponseEntity.ok(history);
//    }
//
//    @GetMapping("/{deviceId}/history")
//    public ResponseEntity<List<HourlyConsumption>> getDailyConsumption(
//            @PathVariable UUID deviceId,
//            @RequestParam("date") long date) {
//
//        return ResponseEntity.ok(monitoringService.getConsumptionForDay(deviceId, date));
//    }
}