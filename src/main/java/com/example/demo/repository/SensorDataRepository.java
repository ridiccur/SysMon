package com.example.demo.repository;

import com.example.demo.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;


// Repository for managing sensor data entities
@Repository
public interface SensorDataRepository
            extends JpaRepository<SensorData, Long> {
            // Find sensor data by bus ID
            List<SensorData> findByBusId(Long busId);

            // Find sensor data by anomaly status
            List<SensorData> findByAnomaly(Boolean anomaly);

            // Find sensor data with anomalies
            List<SensorData> findByAnomalyTrue();

            // Find sensor data after a specific timestamp
            List<SensorData> findByTimestampAfter(LocalDateTime timestamp);

            // Find sensor data before a specific timestamp
            List<SensorData> findByTimestampBefore(LocalDateTime timestamp);

            // Find sensor data between two timestamps
            List<SensorData> findByTimestampBetween(LocalDateTime start,
                LocalDateTime end);
}
