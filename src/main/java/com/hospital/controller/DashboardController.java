package com.hospital.controller;

import com.hospital.repo.AppointmentRepository;
import com.hospital.repo.DoctorRepository;
import com.hospital.repo.PatientRepository;
import com.hospital.repo.PrescriptionRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    private final PrescriptionRepository prescriptions;

    public DashboardController(PatientRepository patients, DoctorRepository doctors,
                               AppointmentRepository appointments, PrescriptionRepository prescriptions) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
        this.prescriptions = prescriptions;
    }

    @GetMapping
    public Map<String, Object> overview() {
        var allAppointments = appointments.findAll();
        long today = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null
                        && a.getAppointmentDate().startsWith(LocalDate.now().toString())).count();
        long scheduled = allAppointments.stream().filter(a -> "Scheduled".equalsIgnoreCase(a.getStatus())).count();
        long completed = allAppointments.stream().filter(a -> "Completed".equalsIgnoreCase(a.getStatus())).count();
        long dispensed = prescriptions.findAll().stream()
                .filter(p -> "Dispensed".equals(p.getStatus())).count();
        return Map.of("patientCount", patients.findAll().size(),
                "doctorCount", doctors.findAll().size(),
                "appointmentCount", allAppointments.size(),
                "todayAppointments", today,
                "scheduledAppointments", scheduled,
                "completedAppointments", completed,
                "prescriptionCount", prescriptions.findAll().size(),
                "dispensedPrescriptions", dispensed,
                "recentAppointments", allAppointments.stream().limit(5).toList());
    }
}
