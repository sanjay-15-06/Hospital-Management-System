package com.hospital.repo;

import com.hospital.model.PatientReport;
import java.util.List;

public interface PatientReportRepository {
    List<PatientReport> findAll();
    PatientReport save(PatientReport report);
}
