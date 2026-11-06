package com.example.internshiptracker.service;

import com.example.internshiptracker.model.*;
import com.example.internshiptracker.repository.InternshipApplicationRepository;
import com.example.internshiptracker.controller.ApplicationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.function.Consumer;

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

    /** Finds possible duplicates by external ID, requisition ID, job URL, or exact company + role. */
    @Transactional(readOnly = true)
    public List<InternshipApplication> lookup(String externalId, String requisitionId, String jobUrl,
                                              String company, String role) {
        return repository.lookup(blankToNull(externalId), blankToNull(requisitionId), blankToNull(jobUrl),
            blankToNull(company), blankToNull(role));
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
        // Optional fields: a missing (null) value keeps the stored value so older clients
        // that only send the original fields do not erase details. An empty string clears it.
        keepUnlessProvided(input.getLocation(), existing::setLocation);
        keepUnlessProvided(input.getJobUrl(), existing::setJobUrl);
        keepUnlessProvided(input.getRequisitionId(), existing::setRequisitionId);
        keepUnlessProvided(input.getResumeVersion(), existing::setResumeVersion);
        keepUnlessProvided(input.getSource(), existing::setSource);
        keepUnlessProvided(input.getVisaNotes(), existing::setVisaNotes);
        keepUnlessProvided(input.getExternalId(), existing::setExternalId);
        if (input.getConfirmedAt() != null) existing.setConfirmedAt(input.getConfirmedAt());
        normalize(existing);
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private static void keepUnlessProvided(String value, Consumer<String> setter) {
        if (value != null) setter.accept(blankToNull(value));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void normalize(InternshipApplication application) {
        application.setCompanyName(application.getCompanyName().trim());
        application.setRoleTitle(application.getRoleTitle().trim());
        application.setLocation(blankToNull(application.getLocation()));
        application.setJobUrl(blankToNull(application.getJobUrl()));
        application.setRequisitionId(blankToNull(application.getRequisitionId()));
        application.setResumeVersion(blankToNull(application.getResumeVersion()));
        application.setSource(blankToNull(application.getSource()));
        application.setVisaNotes(blankToNull(application.getVisaNotes()));
        application.setExternalId(blankToNull(application.getExternalId()));
    }
}
