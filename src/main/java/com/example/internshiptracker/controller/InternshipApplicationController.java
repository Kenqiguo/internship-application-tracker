package com.example.internshiptracker.controller;

import com.example.internshiptracker.model.*;
import com.example.internshiptracker.service.InternshipApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class InternshipApplicationController {
    private final InternshipApplicationService service;

    public InternshipApplicationController(InternshipApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<InternshipApplication> list(@RequestParam(required = false) ApplicationStatus status,
                                           @RequestParam(defaultValue = "") String search) {
        return service.findAll(status, search);
    }

    @GetMapping("/lookup")
    public List<InternshipApplication> lookup(@RequestParam(required = false) String externalId,
                                             @RequestParam(required = false) String requisitionId,
                                             @RequestParam(required = false) String jobUrl,
                                             @RequestParam(required = false) String company,
                                             @RequestParam(required = false) String role) {
        return service.lookup(externalId, requisitionId, jobUrl, company, role);
    }

    @GetMapping("/{id}")
    public InternshipApplication get(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<InternshipApplication> create(@Valid @RequestBody InternshipApplication input) {
        InternshipApplication created = service.create(input);
        return ResponseEntity.created(URI.create("/api/applications/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public InternshipApplication update(@PathVariable Long id, @Valid @RequestBody InternshipApplication input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
