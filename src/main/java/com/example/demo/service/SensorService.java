package com.example.demo.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.dto.AlertDTO;
import com.example.demo.enums.Action;
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
    @Cacheable(value = "sensorDataByBus", key = "#busId")
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
    @Cacheable(value = "sensorDataByTimeRange", key = "{#startTime, #endTime}")
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
    @CacheEvict(value = {"sensorDataByBus", "sensorDataByTimeRange"}, allEntries = true)
    public List<SensorData> saveAllSensorData(List<SensorData> sensorDataList) {
        // Automatic check for anomaly
        for (SensorData sensorData : sensorDataList) {
            boolean isAnomaly = checkForAnomaly(sensorData);
            sensorData.setAnomaly(isAnomaly);
        }
        return sensorDataRepository.saveAll(sensorDataList);
    }

    // Get sensor data by ID
    @Cacheable(value = "sensorData", key = "#id")
    public SensorData getSensorData(Long id) {
        return sensorDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor data not found"));
    }

    // Create new sensor data
    @CacheEvict(value = {"sensorDataByBus", "sensorDataByTimeRange"}, allEntries = true)
    public SensorData createSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }

    // Update existing sensor data
    @CacheEvict(value = {"sensorData", "sensorDataByBus", "sensorDataByTimeRange"}, allEntries = true)
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
    @CacheEvict(value = {"sensorData", "sensorDataByBus", "sensorDataByTimeRange"}, allEntries = true)
    public boolean deleteSensorData(Long id) {
        if (sensorDataRepository.existsById(id)) {
            sensorDataRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Check if sensor data represents an anomaly
    @Cacheable(value = "anomalyCheck", key = "{#sensorData.sensorType, #sensorData.value}")
    public boolean checkForAnomaly(SensorData sensorData) {
        SensorType type = sensorData.getSensorType();
        Double value = sensorData.getValue();

        switch (type) {
            case ENGINE_TEMP:
                // Only check for ERROR conditions (values > 110 or < cold start values)
                return value > 110.0;
            case TIRE_PRESSURE:
                return value > 5.0 || value < 1.5; // ERROR if difference > 0.5 from 4.5
            case FUEL_LEVEL:
                return value < 3.0; // ERROR if fuel level below 3
            default:
                return false;
        }
    }

    // Generate AlertDTO from SensorData
    @Cacheable(value = "alertGeneration", key = "{#sensorData.sensorType, #sensorData.value}")
    public AlertDTO generateAlertFromSensorData(SensorData sensorData) {
        Action action = Action.OK;
        SensorType type = sensorData.getSensorType();
        Double value = sensorData.getValue();

        switch (type) {
            case ENGINE_TEMP:
                // Working range: 80-100 (OK)
                // Warning: difference +-10 degrees (70-80 or 100-110)
                // Error: above 110 degrees
                if ((value >= 80.0 && value <= 100.0)) {
                    action = Action.OK;
                } else if ((value >= 70.0 && value < 80.0) || (value > 100.0 && value <= 110.0)) {
                    action = Action.WARNING;
                } else if (value > 110.0) {
                    action = Action.ERROR;
                }
                break;
            case TIRE_PRESSURE:
                // Working pressure: 4.5
                // Warning: difference +-0.5 (4.0-5.0)
                // Error: difference greater than 0.5
                if (Math.abs(value - 4.5) <= 0.5) {
                    action = Action.OK;
                } else if (Math.abs(value - 4.5) <= 1.0) {
                    action = Action.WARNING;
                } else {
                    action = Action.ERROR;
                }
                break;
            case FUEL_LEVEL:
                // Warning: below 20
                // Error: below 3
                if (value >= 20.0) {
                    action = Action.OK;
                } else if (value >= 3.0) {
                    action = Action.WARNING;
                } else {
                    action = Action.ERROR;
                }
                break;
            default:
                action = Action.OK;
                break;
        }

        return new AlertDTO(sensorData.getId(), sensorData.getSensorType(), action);
    }
}
