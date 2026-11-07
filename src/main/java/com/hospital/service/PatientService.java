package com.hospital.service;

import com.hospital.model.Patient;
import com.hospital.repo.PatientRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PatientService {
    private final PatientRepository repository;
    public PatientService(PatientRepository repository) { this.repository=repository; }
    public List<Patient> getAll() { return repository.findAll(); }
    public Patient getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Patient not found: " + id));
    }
    public Patient save(Patient item) { return repository.save(item); }
    public void delete(Long id) {
        if(!repository.deleteById(id)) throw new NoSuchElementException("Patient not found: " + id);
    }
}
