package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.AlertDTO;
import com.example.demo.dto.CsvImportResult;
import com.example.demo.dto.SensorDataCreateDTO;
import com.example.demo.enums.Action;
import com.example.demo.model.Bus;
import com.example.demo.model.SensorData;
import com.example.demo.model.SensorType;
import com.example.demo.service.BusService;
import com.example.demo.service.CsvImportService;
import com.example.demo.service.SensorService;

@ExtendWith(MockitoExtension.class)
class SensorControllerTest {

    @Mock
    SensorService sensorService;

    @Mock
    BusService busService;

    @Mock
    CsvImportService csvImportService;

    @InjectMocks
    SensorController controller;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createSensorData_success_returnsAlert() {
        SensorDataCreateDTO dto = new SensorDataCreateDTO();
        dto.setBusId(1L);
        dto.setSensorType(SensorType.ENGINE_TEMP);
        dto.setValue(12.5);

        Bus bus = new Bus();
        bus.setId(1L);

        when(busService.getBusById(1L)).thenReturn(Optional.of(bus));

        SensorData saved = new SensorData();
        saved.setId(100L);
        saved.setBus(bus);
        saved.setSensorType(dto.getSensorType());
        saved.setValue(dto.getValue());
        saved.setAnomaly(true);

        when(sensorService.checkForAnomaly(any())).thenReturn(true);
        when(sensorService.createSensorData(any())).thenReturn(saved);

        AlertDTO alert = new AlertDTO(1L, dto.getSensorType(), Action.WARNING);
        when(sensorService.generateAlertFromSensorData(saved)).thenReturn(alert);

        var resp = controller.createSensorData(dto);

        assertThat(resp).isNotNull();
        assertThat(resp.getBody()).isEqualTo(alert);
    }

    @Test
    void getAllSensorData_byType_delegatesToService() {
        SensorData d = new SensorData();
        d.setId(5L);
        d.setSensorType(SensorType.ENGINE_TEMP);

        when(sensorService.getSensorDataByType(SensorType.ENGINE_TEMP)).thenReturn(List.of(d));

        var result = controller.getAllSensorData(Pageable.unpaged(), SensorType.ENGINE_TEMP);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
    }

    @Test
    void importCsv_rejectsNonCsvAndEmptyFile() {
        MockMultipartFile nonCsv = new MockMultipartFile("file", "data.txt", "text/plain", "ok".getBytes());
        var resp1 = controller.importCsv(nonCsv);
        assertThat(resp1.getStatusCode().is4xxClientError()).isTrue();
        CsvImportResult body1 = resp1.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.getFailedCount()).isEqualTo(1);

        MockMultipartFile emptyCsv = new MockMultipartFile("file", "data.csv", "text/csv", new byte[0]);
        var resp2 = controller.importCsv(emptyCsv);
        assertThat(resp2.getStatusCode().is4xxClientError()).isTrue();
        CsvImportResult body2 = resp2.getBody();
        assertThat(body2).isNotNull();
        assertThat(body2.getFailedCount()).isEqualTo(1);
    }

    @Test
    void exportSensorDataToCsv_writesCsvToResponse() throws IOException {
        Bus bus = new Bus();
        bus.setId(11L);

        SensorData d = new SensorData();
        d.setId(7L);
        d.setBus(bus);
        d.setTimestamp(LocalDateTime.of(2023,1,2,3,4));
        d.setSensorType(SensorType.ENGINE_TEMP);
        d.setValue(3.14);
        d.setAnomaly(true);

        when(sensorService.getAll()).thenReturn(List.of(d));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.exportSensorDataToCsv(response);

        assertThat(response.getContentType()).isEqualTo("text/csv");
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=sensor_data.csv");
        String content = response.getContentAsString();
        assertThat(content).contains("busId");
        assertThat(content).contains("11");
    }
}
