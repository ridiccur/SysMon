package com.example.demo.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SensorDataCreateDTO;
import com.example.demo.model.Bus;
import com.example.demo.model.SensorData;
import com.example.demo.service.SensorService;
import com.example.demo.service.BusService;

import jakarta.validation.Valid;;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;
    private final BusService busService; 
    public SensorController(SensorService sensorService, BusService busService) {
        this.sensorService = sensorService;
        this.busService = busService;
    }

    @PostMapping
    public ResponseEntity<SensorData> createSensorData(@RequestBody SensorDataCreateDTO dto) {
        Bus bus = busService.getBusById(dto.getBusId())
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        SensorData sensorData = new SensorData();
        sensorData.setSensorType(dto.getSensorType());
        sensorData.setValue(dto.getValue());
        sensorData.setTimestamp(dto.getTimestamp());
        sensorData.setAnomaly(dto.isAnomaly());
        sensorData.setBus(bus);
        SensorData saved = sensorService.createSensorData(sensorData);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<SensorData> getAllSensorData(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String type) {
        return sensorService.getAll();
    }

    @GetMapping("{busId}")
    public List<SensorData> getSensorDataByBusId(@PathVariable Long busId) {
        return sensorService.getSensorDataByBusId(busId);
    }

    @PutMapping("{id}")
    public ResponseEntity<SensorData> updateSensorData(@PathVariable Long id, @RequestBody @Valid SensorData updatedSensorData) {
        SensorData sensorData = sensorService.updateSensorData(id, updatedSensorData);
        if (sensorData != null) {
            return ResponseEntity.ok(sensorData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSensorData(@PathVariable Long id) {
        boolean deleted = sensorService.deleteSensorData(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
