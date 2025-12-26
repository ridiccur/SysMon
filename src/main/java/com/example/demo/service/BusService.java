package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.example.demo.model.Bus;
import com.example.demo.repository.BusRepository;

// Service class for managing bus entities
@Service
public class BusService {
    private final BusRepository busRepository;
    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    // Get all buses from the database
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    // Get a bus by its ID
    public Optional<Bus> getBusById(Long id) {
        return busRepository.findById(id);
    }

    // Create a new bus
    public Bus createBus(Bus bus) {
        return busRepository.save(bus);
    }

    // Update an existing bus
    public Bus updateBus(Long id, Bus updatedBus) {
        return busRepository.findById(id)
                .map(bus -> {
                    bus.setModel(updatedBus.getModel());
                    return busRepository.save(bus);
                })
                .orElse(null);
    }

    // Delete a bus by its ID
    public boolean deleteBus(Long id) {
        if (busRepository.existsById(id)) {
            busRepository.deleteById(id);
            return true;
        }
        return false;
    }

}