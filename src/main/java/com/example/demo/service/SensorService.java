package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.model.SensorData;
import com.example.demo.model.SensorType;
import com.example.demo.repository.SensorDataRepository;

@Service
public class SensorService {
    private final SensorDataRepository sensorDataRepository;

    public SensorService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    public List<SensorData> getAll() {
        return sensorDataRepository.findAll();
    }

    public List<SensorData> getSensorDataByBusId(Long busId) {
        return sensorDataRepository.findByBusId(busId);
    }

    public List<SensorData> getSensorDataByAnomaly(Boolean anomaly) {
        return sensorDataRepository.findByAnomaly(anomaly);
    }

    public List<SensorData> getAnomalousSensorData() {
        return sensorDataRepository.findByAnomalyTrue();
    }
    
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

    public List<SensorData> saveAllSensorData(List<SensorData> sensorDataList) {
    return sensorDataRepository.saveAll(sensorDataList);
    }

    public SensorData getSensorData(Long id) {
        return sensorDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor data not found"));
    }

    public SensorData createSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }

    public SensorData updateSensorData(Long id, SensorData updatedSensorData) {
        return sensorDataRepository.findById(id)
                .map(sensorData -> {
                    sensorData.setBus(updatedSensorData.getBus());
                    sensorData.setSensorType(updatedSensorData.getSensorType());
                    sensorData.setValue(updatedSensorData.getValue());
                    sensorData.setTimestamp(updatedSensorData.getTimestamp());
                    sensorData.setAnomaly(updatedSensorData.isAnomaly());
                    return sensorDataRepository.save(sensorData);
                })
                .orElse(null);
            }

    public boolean deleteSensorData(Long id) {
        if (sensorDataRepository.existsById(id)) {
            sensorDataRepository.deleteById(id);
            return true;
        }
        return false;
    }

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
