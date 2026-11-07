package com.hospital.repo;

import com.hospital.model.Doctor;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository {
    List<Doctor> findAll();
    Optional<Doctor> findById(Long id);
    Doctor save(Doctor item);
    boolean deleteById(Long id);
}
