package com.example.internshiptracker.repository;

import com.example.internshiptracker.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InternshipApplicationRepository extends JpaRepository<InternshipApplication, Long> {
    // LOCATE treats search text literally, including percent and underscore characters.
    @Query("""
        SELECT a FROM InternshipApplication a
        WHERE (:status IS NULL OR a.status = :status)
          AND (LOCATE(LOWER(:search), LOWER(a.companyName)) > 0
               OR LOCATE(LOWER(:search), LOWER(a.roleTitle)) > 0)
        ORDER BY a.id DESC
        """)
    List<InternshipApplication> search(@Param("status") ApplicationStatus status,
                                      @Param("search") String search);
}
