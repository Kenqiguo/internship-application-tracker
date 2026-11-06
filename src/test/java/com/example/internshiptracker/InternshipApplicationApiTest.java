package com.example.internshiptracker;

import com.example.internshiptracker.repository.InternshipApplicationRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:tracker-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class InternshipApplicationApiTest {
    @LocalServerPort int port;
    @Autowired InternshipApplicationRepository repository;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach void clearDatabase() { repository.deleteAll(); }

    private HttpResponse<String> send(String method, String path, String json) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json))
            .build(), HttpResponse.BodyHandlers.ofString());
    }

    private String input(String company, String role, String status) {
        return """
            {"companyName":"%s","roleTitle":"%s","status":"%s",
             "dateApplied":"2026-10-01","notes":"Follow up next week"}
            """.formatted(company, role, status);
    }

    @Test void completeCrudFlow() throws Exception {
        var created = send("POST", "/api/applications", input(" Example Co ", "Backend Intern", "Applied"));
        assertEquals(201, created.statusCode());
        JsonNode application = mapper.readTree(created.body());
        long id = application.get("id").asLong();
        assertEquals("Example Co", application.get("companyName").asText());
        assertEquals("/api/applications/" + id, created.headers().firstValue("Location").orElseThrow());
        assertEquals(1, mapper.readTree(send("GET", "/api/applications", null).body()).size());
        assertEquals(200, send("GET", "/api/applications/" + id, null).statusCode());
        var updated = send("PUT", "/api/applications/" + id, input("Example Co", "Java Intern", "Interview"));
        assertEquals(200, updated.statusCode());
        assertEquals("Interview", mapper.readTree(updated.body()).get("status").asText());
        assertEquals("Java Intern", mapper.readTree(send("GET", "/api/applications/" + id, null).body()).get("roleTitle").asText());
        assertEquals(204, send("DELETE", "/api/applications/" + id, null).statusCode());
        assertEquals(404, send("GET", "/api/applications/" + id, null).statusCode());
        assertEquals(0, mapper.readTree(send("GET", "/api/applications", null).body()).size());
    }

    @Test void filtersAndSearchCombineAndIgnoreCase() throws Exception {
        send("POST", "/api/applications", input("Acme", "Backend Intern", "Applied"));
        send("POST", "/api/applications", input("Other", "Backend Intern", "Offer"));
        assertEquals(1, mapper.readTree(send("GET", "/api/applications?search=ACME", null).body()).size());
        assertEquals(2, mapper.readTree(send("GET", "/api/applications?search=backend", null).body()).size());
        assertEquals(1, mapper.readTree(send("GET", "/api/applications?status=Applied&search=backend", null).body()).size());
        assertEquals(0, mapper.readTree(send("GET", "/api/applications?status=Rejected", null).body()).size());
        assertEquals(0, mapper.readTree(send("GET", "/api/applications?search=%25", null).body()).size());
    }

    @Test void validatesRequiredFieldsAndLengths() throws Exception {
        var response = send("POST", "/api/applications", "{\"companyName\":\"   \",\"roleTitle\":\"\"}");
        assertEquals(400, response.statusCode());
        JsonNode errors = mapper.readTree(response.body()).get("errors");
        assertTrue(errors.has("companyName"));
        assertTrue(errors.has("roleTitle"));
        assertTrue(errors.has("status"));
        assertEquals(400, send("POST", "/api/applications", input("a".repeat(121), "Intern", "Saved")).statusCode());
        assertEquals(0, repository.count());
    }

    @Test void invalidInputReturnsBadRequest() throws Exception {
        assertEquals(400, send("POST", "/api/applications", "{broken").statusCode());
        assertEquals(400, send("POST", "/api/applications", input("Acme", "Intern", "Unknown")).statusCode());
        assertEquals(400, send("POST", "/api/applications", input("Acme", "Intern", "Saved").replace("2026-10-01", "not-a-date")).statusCode());
        assertEquals(400, send("GET", "/api/applications?status=Unknown", null).statusCode());
        assertEquals(400, send("GET", "/api/applications/not-a-number", null).statusCode());
    }

    @Test void missingUpdatesAndDeletesReturnNotFound() throws Exception {
        assertEquals(404, send("PUT", "/api/applications/999", input("Acme", "Intern", "Saved")).statusCode());
        assertEquals(404, send("DELETE", "/api/applications/999", null).statusCode());
    }

    @Test void savedDateIsOptionalAndClientCannotChooseId() throws Exception {
        var response = send("POST", "/api/applications", """
            {"id":999,"companyName":"Acme","roleTitle":"Intern","status":"Saved"}
            """);
        assertEquals(201, response.statusCode());
        assertNotEquals(999, mapper.readTree(response.body()).get("id").asLong());
        assertTrue(mapper.readTree(response.body()).get("dateApplied").isNull());
        assertEquals(200, send("GET", "/", null).statusCode());
        assertEquals(200, send("GET", "/app.js", null).statusCode());
        assertEquals(200, send("GET", "/styles.css", null).statusCode());
    }

    @Test void newStatusesAndOptionalDetailsAreSaved() throws Exception {
        var created = send("POST", "/api/applications", """
            {"companyName":"Acme","roleTitle":"SWE Intern","status":"OA","dateApplied":"2026-11-01",
             "location":"Remote","jobUrl":"https://example.com/jobs/1","requisitionId":"R-1",
             "resumeVersion":"general_swe","source":"automation","visaNotes":"No sponsorship restriction found",
             "externalId":"job-1"}
            """);
        assertEquals(201, created.statusCode());
        JsonNode body = mapper.readTree(created.body());
        assertEquals("OA", body.get("status").asText());
        assertEquals("R-1", body.get("requisitionId").asText());
        long id = body.get("id").asLong();
        // An older client that only sends the original fields must not erase the details.
        var updated = send("PUT", "/api/applications/" + id, input("Acme", "SWE Intern", "RecruiterReachout"));
        assertEquals(200, updated.statusCode());
        JsonNode after = mapper.readTree(updated.body());
        assertEquals("RecruiterReachout", after.get("status").asText());
        assertEquals("https://example.com/jobs/1", after.get("jobUrl").asText());
        assertEquals("general_swe", after.get("resumeVersion").asText());
    }

    @Test void lookupFindsByAnyIdentifier() throws Exception {
        send("POST", "/api/applications", """
            {"companyName":"Acme","roleTitle":"SWE Intern","status":"Applied","requisitionId":"R-9",
             "jobUrl":"https://example.com/jobs/9","externalId":"job-9"}
            """);
        send("POST", "/api/applications", input("Other", "Data Intern", "Applied"));
        assertEquals(1, mapper.readTree(send("GET", "/api/applications/lookup?requisitionId=R-9", null).body()).size());
        assertEquals(1, mapper.readTree(send("GET", "/api/applications/lookup?externalId=job-9", null).body()).size());
        assertEquals(1, mapper.readTree(send("GET", "/api/applications/lookup?jobUrl=https://example.com/jobs/9", null).body()).size());
        assertEquals(1, mapper.readTree(send("GET", "/api/applications/lookup?company=acme&role=swe%20intern", null).body()).size());
        assertEquals(0, mapper.readTree(send("GET", "/api/applications/lookup?company=acme", null).body()).size());
        assertEquals(0, mapper.readTree(send("GET", "/api/applications/lookup", null).body()).size());
    }
}
