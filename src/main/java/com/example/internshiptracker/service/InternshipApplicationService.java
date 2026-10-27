package com.example.internshiptracker.service;

import com.example.internshiptracker.model.*;
import com.example.internshiptracker.repository.InternshipApplicationRepository;
import com.example.internshiptracker.controller.ApplicationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class InternshipApplicationService {
    private final InternshipApplicationRepository repository;

    public InternshipApplicationService(InternshipApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InternshipApplication> findAll(ApplicationStatus status, String search) {
        return repository.search(status, search == null ? "" : search.trim());
    }

    @Transactional(readOnly = true)
    public InternshipApplication findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    public InternshipApplication create(InternshipApplication application) {
        application.setId(null); // Only the database assigns new IDs.
        normalize(application);
        return repository.save(application);
    }

    public InternshipApplication update(Long id, InternshipApplication input) {
        InternshipApplication existing = findById(id);
        existing.setCompanyName(input.getCompanyName());
        existing.setRoleTitle(input.getRoleTitle());
        existing.setStatus(input.getStatus());
        existing.setDateApplied(input.getDateApplied());
        existing.setNotes(input.getNotes());
        normalize(existing);
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalize(InternshipApplication application) {
        application.setCompanyName(application.getCompanyName().trim());
        application.setRoleTitle(application.getRoleTitle().trim());
    }
}
