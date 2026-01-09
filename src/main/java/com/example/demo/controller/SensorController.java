package com.example.demo.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.AlertDTO;
import com.example.demo.dto.CsvImportResult;
import com.example.demo.dto.SensorDataCreateDTO;
import com.example.demo.model.Bus;
import com.example.demo.model.SensorData;
import com.example.demo.model.SensorType;
import com.example.demo.service.SensorService;
import com.example.demo.service.BusService;
import com.example.demo.service.CsvImportService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controller for managing sensor data entities
@Tag(name = "Sensors controller")
@Slf4j
@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;
    private final BusService busService;
    private final CsvImportService csvImportService;

    public SensorController(SensorService sensorService, BusService busService, CsvImportService csvImportService) {
        this.sensorService = sensorService;
        this.busService = busService;
        this.csvImportService = csvImportService;
    }

    // Create new sensor data with automatic anomaly check and return AlertDTO
    @Operation(summary = "Создать новые данные датчика")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertDTO> createSensorData(@RequestBody SensorDataCreateDTO dto) {
        Bus bus = busService.getBusById(dto.getBusId())
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        SensorData sensorData = new SensorData();
        sensorData.setSensorType(dto.getSensorType());
        sensorData.setValue(dto.getValue());
        sensorData.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp().toLocalDateTime() : java.time.LocalDateTime.now());
        sensorData.setBus(bus);

        // Automatic check for anomaly
        boolean isAnomaly = sensorService.checkForAnomaly(sensorData);
        sensorData.setAnomaly(isAnomaly);

        SensorData saved = sensorService.createSensorData(sensorData);

        // Generate and return AlertDTO
        AlertDTO alert = sensorService.generateAlertFromSensorData(saved);
        return ResponseEntity.ok(alert);
    }

    // Get all sensor data
    @Operation(summary = "Получить все данные датчиков")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SensorData> getAllSensorData(
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false) SensorType sensorType) {
        if (sensorType != null) {
            return sensorService.getSensorDataByType(sensorType);
        }
        return sensorService.getAll();
    }

    // Get sensor data with alerts/anomalies
    @Operation(summary = "Получить данные датчиков с тревогами")
    @GetMapping("alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SensorData> getAlerts() {
        return sensorService.getAnomalousSensorData();
    }

    // Get sensor data by bus ID
    @Operation(summary = "Получить данные датчиков по ID автобуса")
    @GetMapping("{busId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SensorData> getSensorDataByBusId(@PathVariable Long busId) {
        return sensorService.getSensorDataByBusId(busId);
    }

    // Get sensor history by time range
    @Operation(summary = "Получить историю датчиков по диапазону времени")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SensorData> getSensorHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return sensorService.getSensorDataByTimeRange(from, to);
    }

    // Update existing sensor data and return AlertDTO
    @Operation(summary = "Обновить существующие данные датчика")
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertDTO> updateSensorData(@PathVariable Long id, @RequestBody @Valid SensorDataCreateDTO dto) {
        Bus bus = busService.getBusById(dto.getBusId())
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        SensorData updatedSensorData = new SensorData();
        updatedSensorData.setBus(bus);
        updatedSensorData.setSensorType(dto.getSensorType());
        updatedSensorData.setValue(dto.getValue());
        updatedSensorData.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp().toLocalDateTime() : java.time.LocalDateTime.now());

        SensorData sensorData = sensorService.updateSensorData(id, updatedSensorData);
        if (sensorData != null) {
            // Generate and return AlertDTO
            AlertDTO alert = sensorService.generateAlertFromSensorData(sensorData);
            return ResponseEntity.ok(alert);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete sensor data by ID
    @Operation(summary = "Удалить данные датчика по ID")
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSensorData(@PathVariable Long id) {
        boolean deleted = sensorService.deleteSensorData(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Import sensor data from CSV file
    @Operation(summary = "Импорт данных датчиков из CSV файла")
    @PostMapping(value = "/import-csv", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CsvImportResult> importCsv(
            @Parameter(description = "CSV file to upload", required = true)
            @RequestParam("file")
            MultipartFile file) {
        log.info("Received CSV file import request: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        // Check for CSV format
        if (!isCsvFile(file)) {
            CsvImportResult result = new CsvImportResult(0, 1,
                    List.of("File must be in CSV format"));
            return ResponseEntity.badRequest().body(result);
        }

        // Check for empty file
        if (file.isEmpty()) {
            CsvImportResult result = new CsvImportResult(0, 1,
                    List.of("File is empty"));
            return ResponseEntity.badRequest().body(result);
        }

        try {
            CsvImportResult importResult = csvImportService.importProductsFromCsv(file);

            if (importResult.hasError()) {
                log.warn("CSV import completed with {} successes and {} failures",
                        importResult.getSuccessCount(), importResult.getFailedCount());
                return ResponseEntity.unprocessableEntity().body(importResult);
            } else {
                log.info("CSV import successfully completed: {} records imported",
                        importResult.getSuccessCount());
                return ResponseEntity.ok(importResult);
            }

        } catch (Exception e) {
            log.error("Unexpected error during CSV import", e);
            CsvImportResult result = new CsvImportResult(0, 1,
                    List.of("Internal server error: " + e.getMessage()));
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // Check if uploaded file is CSV
    private boolean isCsvFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String contentType = file.getContentType();
        return originalFilename.toLowerCase().endsWith(".csv") ||
               "text/csv".equals(contentType) ||
               "application/vnd.ms-excel".equals(contentType);
    }

    // Export sensor data to CSV file
    @Operation(summary = "Экспорт данных датчиков в CSV файл")
    @GetMapping("/export-csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')"
    )
    public void exportSensorDataToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sensor_data.csv");

        List<SensorData> sensorDataList = sensorService.getAll();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader("busId", "timestamp", "sensorType", "value", "anomaly")
            .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), csvFormat)) {
            for (SensorData data : sensorDataList) {
                csvPrinter.printRecord(
                    data.getBus().getId(),
                    data.getTimestamp(),
                    data.getSensorType(),
                    data.getValue(),
                    data.isAnomaly()
                );
            }
        }
    }

    // Export sensor data to XLSX (Excel)
    @Operation(summary = "Экспорт данных датчиков в XLSX файл")
    @GetMapping("/export-xlsx")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void exportSensorDataToXlsx(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sensor_data.xlsx");

        List<SensorData> sensorDataList = sensorService.getAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Sensor Data");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Bus ID");
            header.createCell(2).setCellValue("Timestamp");
            header.createCell(3).setCellValue("Sensor Type");
            header.createCell(4).setCellValue("Value");
            header.createCell(5).setCellValue("Anomaly");

            // Date cell style
            CellStyle dateStyle = workbook.createCellStyle();
            short dateFormat = createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss");
            dateStyle.setDataFormat(dateFormat);

            int rowIdx = 1;
            for (SensorData data : sensorDataList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(data.getId() != null ? data.getId() : 0);
                Long busId = null;
                if (data.getBus() != null) busId = data.getBus().getId();
                row.createCell(1).setCellValue(busId != null ? busId : 0);

                Cell tsCell = row.createCell(2);
                if (data.getTimestamp() != null) {
                    // write as string formatted
                    tsCell.setCellValue(data.getTimestamp().toString());
                    tsCell.setCellStyle(dateStyle);
                } else {
                    tsCell.setCellValue("");
                }

                row.createCell(3).setCellValue(data.getSensorType() != null ? data.getSensorType().name() : "");
                row.createCell(4).setCellValue(data.getValue() != null ? data.getValue() : 0.0);
                row.createCell(5).setCellValue(data.isAnomaly());
            }

            // Autosize columns
            for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }
}
