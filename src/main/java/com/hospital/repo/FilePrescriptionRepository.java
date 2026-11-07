package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Prescription;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FilePrescriptionRepository implements PrescriptionRepository {
    private final JsonFileStore<Prescription> store;
    public FilePrescriptionRepository(ObjectMapper mapper) {
        store = new JsonFileStore<>("prescriptions.json", mapper,
                new TypeReference<List<Prescription>>() {}, Prescription::getId);
    }
    public List<Prescription> findAll() { return store.findAll(); }
    public Optional<Prescription> findById(Long id) { return store.findById(id); }
    public Prescription save(Prescription item) { return store.save(item); }
}
