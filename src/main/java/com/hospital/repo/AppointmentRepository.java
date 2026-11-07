package com.hospital.repo;

import com.hospital.model.Appointment;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<Appointment> findAll();
    Optional<Appointment> findById(Long id);
    Appointment save(Appointment item);
    boolean deleteById(Long id);
}
