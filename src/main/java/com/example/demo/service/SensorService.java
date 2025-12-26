package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.model.SensorData;
import com.example.demo.model.SensorType;
import com.example.demo.repository.SensorDataRepository;

// Service class for managing sensor data entities
@Service
public class SensorService {
    private final SensorDataRepository sensorDataRepository;

    public SensorService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    // Get all sensor data from the database
    public List<SensorData> getAll() {
        return sensorDataRepository.findAll();
    }

    // Get sensor data by bus ID
    public List<SensorData> getSensorDataByBusId(Long busId) {
        return sensorDataRepository.findByBusId(busId);
    }

    // Get sensor data by anomaly status
    public List<SensorData> getSensorDataByAnomaly(Boolean anomaly) {
        return sensorDataRepository.findByAnomaly(anomaly);
    }

    // Get sensor data with anomalies
    public List<SensorData> getAnomalousSensorData() {
        return sensorDataRepository.findByAnomalyTrue();
    }

    // Get sensor data by time range
    public List<SensorData> getSensorDataByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null && endTime == null) {
            return sensorDataRepository.findAll();
        } else if (startTime == null) {
            return sensorDataRepository.findByTimestampBefore(endTime);
        } else if (endTime == null) {
            return sensorDataRepository.findByTimestampAfter(startTime);
        } else {
            return sensorDataRepository.findByTimestampBetween(startTime, endTime);
        }
    }

    // Save all sensor data with automatic anomaly checking
    public List<SensorData> saveAllSensorData(List<SensorData> sensorDataList) {
        // Automatic check for anomaly
        for (SensorData sensorData : sensorDataList) {
            boolean isAnomaly = checkForAnomaly(sensorData);
            sensorData.setAnomaly(isAnomaly);
        }
        return sensorDataRepository.saveAll(sensorDataList);
    }

    // Get sensor data by ID
    public SensorData getSensorData(Long id) {
        return sensorDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor data not found"));
    }

    // Create new sensor data
    public SensorData createSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }

    // Update existing sensor data
    public SensorData updateSensorData(Long id, SensorData updatedSensorData) {
        return sensorDataRepository.findById(id)
                .map(sensorData -> {
                    sensorData.setBus(updatedSensorData.getBus());
                    sensorData.setSensorType(updatedSensorData.getSensorType());
                    sensorData.setValue(updatedSensorData.getValue());
                    sensorData.setTimestamp(updatedSensorData.getTimestamp());

                    // Automatic check for anomaly with updating
                    boolean isAnomaly = checkForAnomaly(sensorData);
                    sensorData.setAnomaly(isAnomaly);

                    return sensorDataRepository.save(sensorData);
                })
                .orElse(null);
            }

    // Delete sensor data by ID
    public boolean deleteSensorData(Long id) {
        if (sensorDataRepository.existsById(id)) {
            sensorDataRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Check if sensor data represents an anomaly
    public boolean checkForAnomaly(SensorData sensorData) {
    SensorType type = sensorData.getSensorType();
    Double value = sensorData.getValue();

    switch (type) {
        case ENGINE_TEMP:
            return value > 100.0 || value < 60.0;
        case TIRE_PRESSURE:
            return value > 3.5 || value < 1.8;
        case FUEL_LEVEL:
            return value < 5.0;
        default:
            return false;
        }
    }
}
