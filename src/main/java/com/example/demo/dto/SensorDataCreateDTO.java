package com.example.demo.dto;

import java.time.OffsetDateTime;

import com.example.demo.model.SensorType;

import lombok.Data;

@Data
public class SensorDataCreateDTO {
    private SensorType sensorType;
    private Double value;
    private OffsetDateTime timestamp;
    private Long busId;
}