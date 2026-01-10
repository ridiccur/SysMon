package com.example.demo.controller;

import com.example.demo.model.Bus;
import com.example.demo.service.BusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

// Controller for managing bus entities
@Tag(name = "Bus controller")
@Slf4j
@RestController
@RequestMapping("/api/buses")
public class BusController {
    @Autowired
    private BusService busService;

    // Get all buses
    @Operation(summary = "Получить все автобусы")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Bus>> getAllBuses() {
        log.info("Получение всех автобусов");
        List<Bus> buses = busService.getAllBuses();
        log.info("Найдено {} автобусов", buses.size());
        return ResponseEntity.ok(buses);
    }

    // Create a new bus
    @Operation(summary = "Создать новый автобус")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Bus> createBus(@RequestBody Bus bus) {
        log.info("Создание нового автобуса: {}", bus);
        Bus createdBus = busService.createBus(bus);
        log.info("Автобус создан с ID: {}", createdBus.getId());
        return ResponseEntity.ok(createdBus);
    }

    // Update an existing bus
    @Operation(summary = "Обновить существующий автобус")
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Bus> updateBus(@PathVariable Long id, @RequestBody Bus bus) {
        log.info("Обновление автобуса с ID: {}", id);
        Bus updatedBus = busService.updateBus(id, bus);
        if (updatedBus != null) {
            log.info("Автобус с ID {} успешно обновлен", id);
            return ResponseEntity.ok(updatedBus);
        } else {
            log.warn("Автобус с ID {} не найден для обновления", id);
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a bus by ID
    @Operation(summary = "Удалить автобус по ID")
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBus(@PathVariable Long id) {
        log.info("Удаление автобуса с ID: {}", id);
        boolean deleted = busService.deleteBus(id);
        if (deleted) {
            log.info("Автобус с ID {} успешно удален", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Автобус с ID {} не найден для удаления", id);
            return ResponseEntity.notFound().build();
        }
    }
}