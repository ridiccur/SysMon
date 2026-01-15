package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.example.demo.model.Bus;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    SensorService sensorService;

    @Mock
    BusService busService;

    @InjectMocks
    CsvImportService service;

    @Test
    void import_validCsv_parsedAndSaved() throws Exception {
        // omit anomaly column so sensorService.checkForAnomaly(...) is invoked
        String csv = "busId,timestamp,sensorType,value\n1,2026-01-01T00:00:00,ENGINE_TEMP,90\n";
        MockMultipartFile file = new MockMultipartFile("file", "in.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        Bus bus = new Bus(); bus.setId(1L);
        when(busService.getBusById(1L)).thenReturn(Optional.of(bus));

        when(sensorService.checkForAnomaly(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        var result = service.importProductsFromCsv(file);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailedCount()).isEqualTo(0);
    }

    @Test
    void import_invalidCsv_returnsErrors() throws Exception {
        String csv = "busId,timestamp,sensorType,value\n,invalid,FOO,abc\n";
        MockMultipartFile file = new MockMultipartFile("file", "in.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        // no stubbing for busService because CSV has missing busId and parsing will fail earlier
        var result = service.importProductsFromCsv(file);
        assertThat(result.getFailedCount()).isGreaterThanOrEqualTo(1);
    }
}
