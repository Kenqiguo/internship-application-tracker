package com.example.internshiptracker;

import com.example.internshiptracker.model.*;
import com.example.internshiptracker.repository.InternshipApplicationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import java.time.LocalDate;

@Configuration
@Profile("demo")
public class DemoData {
    @Bean
    CommandLineRunner seedDemo(InternshipApplicationRepository repository) {
        return args -> {
            if (repository.count() != 0) return;
            String[] companies = {"Maple Orbit Labs", "Cedar Pixel Studio", "Blue Lantern Systems", "Silver Fern Cloud", "Paper Kite Robotics"};
            ApplicationStatus[] statuses = ApplicationStatus.values();
            for (int i = 0; i < companies.length; i++) {
                var application = new InternshipApplication();
                application.setCompanyName(companies[i]);
                application.setRoleTitle(i % 2 == 0 ? "Java Backend Intern" : "Software Engineering Intern");
                application.setStatus(statuses[i]);
                application.setDateApplied(i == 0 ? null : LocalDate.of(2026, 11, i));
                application.setNotes("Fictional demo application. No real applicant or employer data.");
                repository.save(application);
            }
        };
    }
}
