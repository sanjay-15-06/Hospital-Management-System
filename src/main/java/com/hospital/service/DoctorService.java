package com.hospital.service;

import com.hospital.model.Doctor;
import com.hospital.repo.DoctorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DoctorService {
    private final DoctorRepository repository;
    public DoctorService(DoctorRepository repository) { this.repository=repository; }
    public List<Doctor> getAll() { return repository.findAll(); }
    public Doctor getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Doctor not found: " + id));
    }
    public Doctor save(Doctor item) { return repository.save(item); }
    public void delete(Long id) {
        if(!repository.deleteById(id)) throw new NoSuchElementException("Doctor not found: " + id);
    }
}
