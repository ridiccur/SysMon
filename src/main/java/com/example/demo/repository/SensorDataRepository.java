package com.example.demo.repository;

import com.example.demo.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;


@Repository
public interface SensorDataRepository
            extends JpaRepository<SensorData, Long> {
            List<SensorData> findByBusId(Long busId);

            List<SensorData> findByAnomaly(Boolean anomaly);
            
            List<SensorData> findByAnomalyTrue();

            List<SensorData> findByTimestampAfter(LocalDateTime timestamp);

            List<SensorData> findByTimestampBefore(LocalDateTime timestamp);

            List<SensorData> findByTimestampBetween(LocalDateTime start, 
                LocalDateTime end);
}
