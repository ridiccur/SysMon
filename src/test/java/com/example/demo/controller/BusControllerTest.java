package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.demo.model.Bus;
import com.example.demo.service.BusService;

@ExtendWith(MockitoExtension.class)
class BusControllerTest {

    @Mock
    BusService busService;

    @InjectMocks
    BusController controller;

    @Test
    void getAllBuses_returnsOk() {
        Bus b = new Bus(); b.setId(1L); b.setModel("M");
        when(busService.getAllBuses()).thenReturn(List.of(b));
        ResponseEntity<List<Bus>> resp = controller.getAllBuses();
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void createBus_returnsCreated() {
        Bus b = new Bus(); b.setModel("X");
        Bus saved = new Bus(); saved.setId(2L); saved.setModel("X");
        when(busService.createBus(b)).thenReturn(saved);
        var resp = controller.createBus(b);
        assertThat(resp.getBody().getId()).isEqualTo(2L);
    }

    @Test
    void updateBus_notFound_returns404() {
        Bus updated = new Bus(); updated.setModel("B");
        when(busService.updateBus(99L, updated)).thenReturn(null);
        var resp = controller.updateBus(99L, updated);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }
}
