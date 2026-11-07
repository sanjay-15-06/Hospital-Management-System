package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {
    private final DoctorService service;
    public DoctorController(DoctorService service) { this.service=service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor insert(@RequestBody Doctor data) { return service.save(data); }

    @GetMapping
    public List<Doctor> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public Doctor edit(@PathVariable Long id, @RequestBody Doctor data) {
        data.setId(id);
        return service.save(data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
