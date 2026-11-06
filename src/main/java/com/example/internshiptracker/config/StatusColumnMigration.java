package com.example.internshiptracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Older databases created the status column as an H2 ENUM limited to the original five statuses.
 * Convert it to VARCHAR once so new statuses (OA, RecruiterReachout) can be saved. Existing values
 * are kept. Runs after Hibernate's schema update but before the web server accepts requests.
 */
@Component
public class StatusColumnMigration implements SmartInitializingSingleton {
    private static final Logger log = LoggerFactory.getLogger(StatusColumnMigration.class);
    private final JdbcTemplate jdbc;

    public StatusColumnMigration(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            List<String> types = jdbc.queryForList("""
                SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'INTERNSHIP_APPLICATION' AND COLUMN_NAME = 'STATUS'
                """, String.class);
            if (!types.isEmpty() && "ENUM".equalsIgnoreCase(types.get(0))) {
                jdbc.execute("ALTER TABLE INTERNSHIP_APPLICATION ALTER COLUMN STATUS SET DATA TYPE VARCHAR(32)");
                log.info("Converted INTERNSHIP_APPLICATION.STATUS from ENUM to VARCHAR(32)");
            }
        } catch (Exception e) {
            // Non-H2 databases or unexpected schemas: leave the column alone.
            log.warn("Status column migration skipped: {}", e.getMessage());
        }
    }
}
