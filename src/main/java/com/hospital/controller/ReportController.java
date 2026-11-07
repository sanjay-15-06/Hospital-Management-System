package com.hospital.controller;

import com.hospital.model.PatientReport;
import com.hospital.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientReport create(@RequestBody PatientReport report) { return service.create(report); }

    @GetMapping
    public List<PatientReport> getAll() { return service.getAll(); }

    @GetMapping("/patient/{patientId}")
    public List<PatientReport> forPatient(@PathVariable Long patientId) { return service.forPatient(patientId); }
}
