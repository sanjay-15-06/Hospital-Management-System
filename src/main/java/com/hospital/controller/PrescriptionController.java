package com.hospital.controller;

import com.hospital.model.Prescription;
import com.hospital.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService service;
    public PrescriptionController(PrescriptionService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prescription create(@RequestBody Prescription prescription) { return service.create(prescription); }

    @GetMapping
    public List<Prescription> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Prescription getById(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/patient/{patientId}")
    public List<Prescription> forPatient(@PathVariable Long patientId) { return service.forPatient(patientId); }
}
