package com.example.internshiptracker;

import com.example.internshiptracker.config.StatusColumnMigration;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;

class StatusColumnMigrationTest {
    @Test void convertsLegacyEnumColumnAndKeepsValues() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:legacy;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE INTERNSHIP_APPLICATION (ID BIGINT PRIMARY KEY, " +
            "STATUS ENUM('Saved','Applied','Interview','Offer','Rejected') NOT NULL)");
        jdbc.update("INSERT INTO INTERNSHIP_APPLICATION VALUES (1, 'Applied')");
        assertThrows(Exception.class, () -> jdbc.update("INSERT INTO INTERNSHIP_APPLICATION VALUES (2, 'OA')"));

        new StatusColumnMigration(jdbc).afterSingletonsInstantiated();

        jdbc.update("INSERT INTO INTERNSHIP_APPLICATION VALUES (2, 'OA')");
        assertEquals("Applied", jdbc.queryForObject("SELECT STATUS FROM INTERNSHIP_APPLICATION WHERE ID = 1", String.class));
        assertEquals("OA", jdbc.queryForObject("SELECT STATUS FROM INTERNSHIP_APPLICATION WHERE ID = 2", String.class));
        new StatusColumnMigration(jdbc).afterSingletonsInstantiated(); // second run is a no-op
    }
}
