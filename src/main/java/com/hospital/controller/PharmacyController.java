package com.hospital.controller;

import com.hospital.model.Prescription;
import com.hospital.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {
    private final PrescriptionService service;
    public PharmacyController(PrescriptionService service) { this.service = service; }

    @GetMapping("/prescriptions")
    public List<Prescription> lookup(@RequestParam String search) {
        return service.findForPharmacy(search);
    }

    @PostMapping("/prescriptions/{id}/dispense")
    public Prescription dispense(@PathVariable Long id) { return service.dispense(id); }
}
