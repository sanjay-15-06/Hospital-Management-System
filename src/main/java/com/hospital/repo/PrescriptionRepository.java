package com.hospital.repo;

import com.hospital.model.Prescription;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository {
    List<Prescription> findAll();
    Optional<Prescription> findById(Long id);
    Prescription save(Prescription item);
}
