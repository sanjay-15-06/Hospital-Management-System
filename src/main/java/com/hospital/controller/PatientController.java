package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin
public class PatientController {
    private final PatientService service;
    public PatientController(PatientService service) { this.service=service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient insert(@RequestBody Patient data) { return service.save(data); }

    @GetMapping
    public List<Patient> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public Patient edit(@PathVariable Long id, @RequestBody Patient data) {
        data.setId(id);
        return service.save(data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
