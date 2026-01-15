package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.model.Bus;
import com.example.demo.repository.BusRepository;

@ExtendWith(MockitoExtension.class)
class BusServiceTest {

    @Mock
    BusRepository repository;

    @InjectMocks
    BusService service;

    @Test
    void createAndGetBus() {
        Bus bus = new Bus();
        bus.setId(10L);
        bus.setModel("X");

        when(repository.save(bus)).thenReturn(bus);
        when(repository.findById(10L)).thenReturn(Optional.of(bus));

        Bus created = service.createBus(bus);
        assertThat(created).isEqualTo(bus);

        Optional<Bus> got = service.getBusById(10L);
        assertThat(got).isPresent();
        assertThat(got.get().getId()).isEqualTo(10L);
    }

    @Test
    void updateBus_returnsUpdatedOrNull() {
        Bus existing = new Bus();
        existing.setId(5L);
        existing.setModel("A");

        Bus updated = new Bus();
        updated.setModel("B");

        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenAnswer(i -> i.getArgument(0));

        Bus res = service.updateBus(5L, updated);
        assertThat(res.getModel()).isEqualTo("B");

        when(repository.findById(99L)).thenReturn(Optional.empty());
        Bus none = service.updateBus(99L, updated);
        assertThat(none).isNull();
    }
}
