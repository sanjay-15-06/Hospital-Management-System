package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prescription {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private String patientName;
    private String patientSummary;
    private String diagnosis;
    private String notes;
    private String status = "Ready";
    private String createdAt = LocalDateTime.now().toString();
    private String dispensedAt;
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientSummary() { return patientSummary; }
    public void setPatientSummary(String patientSummary) { this.patientSummary = patientSummary; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getDispensedAt() { return dispensedAt; }
    public void setDispensedAt(String dispensedAt) { this.dispensedAt = dispensedAt; }
    public List<PrescriptionItem> getItems() { return items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items == null ? new ArrayList<>() : items; }
}
