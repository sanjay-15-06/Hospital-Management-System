package com.hospital.service;

import com.hospital.model.Appointment;
import com.hospital.repo.AppointmentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    public AppointmentService(AppointmentRepository repository) { this.repository=repository; }
    public List<Appointment> getAll() { return repository.findAll(); }
    public Appointment getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Appointment not found: " + id));
    }
    public Appointment save(Appointment item) { return repository.save(item); }
    public void delete(Long id) {
        if(!repository.deleteById(id)) throw new NoSuchElementException("Appointment not found: " + id);
    }
}
