package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.List;
import com.example.demo.model.SensorData;
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
                    sensorData.setBusId(updatedSensorData.getBusId());
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



}
