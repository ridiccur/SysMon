package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.AlertDTO;
import com.example.demo.enums.Action;
import com.example.demo.model.SensorData;
import com.example.demo.model.SensorType;
import com.example.demo.repository.SensorDataRepository;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    SensorDataRepository repository;

    @InjectMocks
    SensorService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void checkForAnomaly_engineTemp_over110_true() {
        SensorData d = new SensorData();
        d.setSensorType(SensorType.ENGINE_TEMP);
        d.setValue(120.0);

        boolean result = service.checkForAnomaly(d);
        assertThat(result).isTrue();
    }

    @Test
    void generateAlert_engineTemp_ranges() {
        SensorData ok = new SensorData(1L, null, SensorType.ENGINE_TEMP, 90.0, LocalDateTime.now(), false);
        AlertDTO aOk = service.generateAlertFromSensorData(ok);
        assertThat(aOk.getAction()).isEqualTo(Action.OK);

        SensorData warn = new SensorData(2L, null, SensorType.ENGINE_TEMP, 75.0, LocalDateTime.now(), false);
        AlertDTO aWarn = service.generateAlertFromSensorData(warn);
        assertThat(aWarn.getAction()).isEqualTo(Action.WARNING);

        SensorData err = new SensorData(3L, null, SensorType.ENGINE_TEMP, 120.0, LocalDateTime.now(), false);
        AlertDTO aErr = service.generateAlertFromSensorData(err);
        assertThat(aErr.getAction()).isEqualTo(Action.ERROR);
    }

    @Test
    void getSensorDataByType_delegatesToRepository() {
        SensorData d = new SensorData();
        d.setSensorType(SensorType.FUEL_LEVEL);
        when(repository.findBySensorType(SensorType.FUEL_LEVEL)).thenReturn(List.of(d));

        var res = service.getSensorDataByType(SensorType.FUEL_LEVEL);
        assertThat(res).hasSize(1);
    }
}
