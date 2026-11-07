package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.PatientReport;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class FilePatientReportRepository implements PatientReportRepository {
    private final JsonFileStore<PatientReport> store;
    public FilePatientReportRepository(ObjectMapper mapper) {
        store = new JsonFileStore<>("reports.json", mapper,
                new TypeReference<List<PatientReport>>() {}, PatientReport::getId);
    }
    public List<PatientReport> findAll() { return store.findAll(); }
    public PatientReport save(PatientReport report) { return store.save(report); }
}
