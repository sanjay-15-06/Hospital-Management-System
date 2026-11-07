package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Patient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FilePatientRepository implements PatientRepository {
    private final JsonFileStore<Patient> store;
    public FilePatientRepository(ObjectMapper mapper) {
        this.store=new JsonFileStore<>("patients.json", mapper, new TypeReference<List<Patient>>() {}, Patient::getId);
    }
    public List<Patient> findAll() { return store.findAll(); }
    public Optional<Patient> findById(Long id) { return store.findById(id); }
    public Patient save(Patient item) { return store.save(item); }
    public boolean deleteById(Long id) { return store.deleteById(id); }
}
