package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Doctor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FileDoctorRepository implements DoctorRepository {
    private final JsonFileStore<Doctor> store;
    public FileDoctorRepository(ObjectMapper mapper) {
        this.store=new JsonFileStore<>("doctors.json", mapper, new TypeReference<List<Doctor>>() {}, Doctor::getId);
    }
    public List<Doctor> findAll() { return store.findAll(); }
    public Optional<Doctor> findById(Long id) { return store.findById(id); }
    public Doctor save(Doctor item) { return store.save(item); }
    public boolean deleteById(Long id) { return store.deleteById(id); }
}
