package com.hospital.repo;

import com.hospital.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    List<Patient> findAll();
    Optional<Patient> findById(Long id);
    Patient save(Patient item);
    boolean deleteById(Long id);
}
