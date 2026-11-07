package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Appointment;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FileAppointmentRepository implements AppointmentRepository {
    private final JsonFileStore<Appointment> store;
    public FileAppointmentRepository(ObjectMapper mapper) {
        this.store=new JsonFileStore<>("appointments.json", mapper, new TypeReference<List<Appointment>>() {}, Appointment::getId);
    }
    public List<Appointment> findAll() { return store.findAll(); }
    public Optional<Appointment> findById(Long id) { return store.findById(id); }
    public Appointment save(Appointment item) { return store.save(item); }
    public boolean deleteById(Long id) { return store.deleteById(id); }
}
