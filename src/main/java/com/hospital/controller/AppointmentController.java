package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service=service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment insert(@RequestBody Appointment data) { return service.save(data); }

    @GetMapping
    public List<Appointment> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Appointment getById(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public Appointment edit(@PathVariable Long id, @RequestBody Appointment data) {
        data.setId(id);
        return service.save(data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
