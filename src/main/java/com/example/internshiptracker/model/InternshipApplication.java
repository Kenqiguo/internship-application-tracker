package com.example.internshiptracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

@Entity
public class InternshipApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String companyName;

    @NotBlank(message = "Role title is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String roleTitle;

    // Stored as text so new statuses can be added without a database enum change.
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status;

    // Saved applications may not have an application date yet.
    private LocalDate dateApplied;

    @Size(max = 2000)
    @Column(length = 2000)
    private String notes;

    // Optional details. All nullable so older records and clients keep working.
    @Size(max = 255)
    private String location;

    @Size(max = 1000)
    @Column(length = 1000)
    private String jobUrl;

    @Size(max = 120)
    @Column(length = 120)
    private String requisitionId;

    @Size(max = 60)
    @Column(length = 60)
    private String resumeVersion;

    @Size(max = 120)
    @Column(length = 120)
    private String source;

    @Size(max = 1000)
    @Column(length = 1000)
    private String visaNotes;

    // ID assigned by an external tool (for example the job automation) to avoid duplicates.
    @Size(max = 120)
    @Column(length = 120)
    private String externalId;

    private LocalDate confirmedAt;

    public InternshipApplication() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDate getDateApplied() { return dateApplied; }
    public void setDateApplied(LocalDate dateApplied) { this.dateApplied = dateApplied; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    public String getRequisitionId() { return requisitionId; }
    public void setRequisitionId(String requisitionId) { this.requisitionId = requisitionId; }
    public String getResumeVersion() { return resumeVersion; }
    public void setResumeVersion(String resumeVersion) { this.resumeVersion = resumeVersion; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getVisaNotes() { return visaNotes; }
    public void setVisaNotes(String visaNotes) { this.visaNotes = visaNotes; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public LocalDate getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDate confirmedAt) { this.confirmedAt = confirmedAt; }
}
