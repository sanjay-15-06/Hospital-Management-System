package com.hospital.service;

import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import com.hospital.repo.PatientRepository;
import com.hospital.repo.PrescriptionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class PrescriptionService {
    private static final Set<String> FOOD_TIMINGS = Set.of("Before food", "After food", "With food", "Any time");
    private static final Set<String> TIMES = Set.of("Morning", "Afternoon", "Evening", "Night");
    private final PrescriptionRepository repository;
    private final PatientRepository patients;
    private final DoctorService doctors;

    public PrescriptionService(PrescriptionRepository repository, PatientRepository patients, DoctorService doctors) {
        this.repository = repository;
        this.patients = patients;
        this.doctors = doctors;
    }

    public List<Prescription> getAll() { return repository.findAll(); }
    public Prescription getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Prescription not found: " + id));
    }
    public List<Prescription> forPatient(Long patientId) {
        return repository.findAll().stream().filter(p -> patientId.equals(p.getPatientId())).toList();
    }
    public Prescription create(Prescription item) {
        if (item.getPatientId() == null) throw new IllegalArgumentException("A patient is required.");
        var patient = patients.findById(item.getPatientId()).orElseThrow(
                () -> new NoSuchElementException("Patient not found: " + item.getPatientId()));
        if (item.getDoctorId() == null) throw new IllegalArgumentException("A doctor is required.");
        doctors.getById(item.getDoctorId());
        if (item.getItems() == null || item.getItems().isEmpty()) {
            throw new IllegalArgumentException("Add at least one medicine.");
        }
        for (PrescriptionItem medicine : item.getItems()) validateMedicine(medicine);
        item.setId(null);
        item.setStatus("Ready");
        item.setPatientName(patient.getName());
        item.setCreatedAt(LocalDateTime.now().toString());
        item.setDispensedAt(null);
        return repository.save(item);
    }

    public List<Prescription> findForPharmacy(String search) {
        if (search == null || search.isBlank()) throw new IllegalArgumentException("Enter a patient ID or name.");
        String query = search.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream().filter(p -> {
            if (String.valueOf(p.getPatientId()).equals(query)) return true;
            return patients.findById(p.getPatientId())
                    .map(patient -> patient.getName() != null
                            && patient.getName().toLowerCase(Locale.ROOT).contains(query))
                    .orElse(false);
        }).toList();
    }

    public Prescription dispense(Long id) {
        Prescription prescription = getById(id);
        if ("Dispensed".equals(prescription.getStatus())) {
            throw new IllegalArgumentException("This prescription has already been dispensed.");
        }
        prescription.setStatus("Dispensed");
        prescription.setDispensedAt(LocalDateTime.now().toString());
        return repository.save(prescription);
    }

    private void validateMedicine(PrescriptionItem item) {
        if (item.getMedicineName() == null || item.getMedicineName().isBlank()) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        if (item.getDurationDays() < 1 || item.getQuantity() < 1) {
            throw new IllegalArgumentException("Medicine duration and quantity must be at least one.");
        }
        if (item.getFoodTiming() == null || !FOOD_TIMINGS.contains(item.getFoodTiming())) {
            throw new IllegalArgumentException("Select a valid food timing.");
        }
        if (item.getTimes() == null || item.getTimes().isEmpty()
                || !TIMES.containsAll(item.getTimes())) {
            throw new IllegalArgumentException("Select one or more valid times of day.");
        }
    }
}
