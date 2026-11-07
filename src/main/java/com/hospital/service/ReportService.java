package com.hospital.service;

import com.hospital.model.PatientReport;
import com.hospital.repo.PatientReportRepository;
import com.hospital.repo.PatientRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReportService {
    private final PatientReportRepository reports;
    private final PatientRepository patients;
    private final DoctorService doctors;

    public ReportService(PatientReportRepository reports, PatientRepository patients, DoctorService doctors) {
        this.reports = reports;
        this.patients = patients;
        this.doctors = doctors;
    }

    public List<PatientReport> getAll() { return reports.findAll(); }
    public List<PatientReport> forPatient(Long patientId) {
        return reports.findAll().stream().filter(r -> patientId.equals(r.getPatientId())).toList();
    }
    public PatientReport create(PatientReport report) {
        if (report.getPatientId() == null) throw new IllegalArgumentException("A patient is required.");
        patients.findById(report.getPatientId()).orElseThrow(
                () -> new NoSuchElementException("Patient not found: " + report.getPatientId()));
        if (report.getDoctorId() == null) throw new IllegalArgumentException("A doctor is required.");
        doctors.getById(report.getDoctorId());
        if (report.getSummary() == null || report.getSummary().isBlank()) {
            throw new IllegalArgumentException("A visit summary is required.");
        }
        report.setId(null);
        report.setCreatedAt(LocalDateTime.now().toString());
        return reports.save(report);
    }
}
