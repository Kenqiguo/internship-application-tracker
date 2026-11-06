package com.example.internshiptracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:demo-profile-test;DB_CLOSE_DELAY=-1", "app.max-records=6"
})
class DemoProfileTest {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper mapper = JsonMapper.builder().build();

    private HttpResponse<String> send(String method, String path, String json) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json))
            .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void demoIsOpenSeededFictionalAndCapped() throws Exception {
        JsonNode seeded = mapper.readTree(send("GET", "/api/applications", null).body());
        assertEquals(5, seeded.size());
        for (JsonNode a : seeded) assertTrue(a.get("notes").asText().startsWith("Fictional demo application"));
        assertEquals("demo", mapper.readTree(send("GET", "/api/session", null).body()).get("mode").asText());
        assertEquals(200, send("GET", "/healthz", null).statusCode());

        String body = "{\"companyName\":\"Visitor Co\",\"roleTitle\":\"Intern\",\"status\":\"Applied\"}";
        assertEquals(201, send("POST", "/api/applications", body).statusCode());   // 6th record: allowed
        var full = send("POST", "/api/applications", body);                         // 7th: over the cap
        assertEquals(429, full.statusCode());
        assertTrue(mapper.readTree(full.body()).get("message").asText().contains("at most 6"));
    }
}
