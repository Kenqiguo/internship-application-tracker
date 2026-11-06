package com.example.internshiptracker;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("private")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
 "spring.datasource.url=jdbc:h2:mem:private-test;DB_CLOSE_DELAY=-1", "spring.datasource.username=sa",
 "spring.datasource.password=", "spring.sql.init.mode=never", "spring.jpa.properties.hibernate.default_schema=PUBLIC", "server.servlet.session.cookie.secure=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PrivateAccessTest {
 @LocalServerPort int port;
 static final String key="test-only-long-muse-token";
 @DynamicPropertySource static void properties(DynamicPropertyRegistry r) throws Exception {
   r.add("TRACKER_PASSWORD_HASH", () -> "{bcrypt}"+new BCryptPasswordEncoder().encode("test-owner-password"));
   String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)));
   r.add("MUSE_TOKEN_SHA256", () -> hash);
 }
 HttpResponse<String> request(HttpClient client,String method,String path,String body,String... headers) throws Exception {
   var builder=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body));
   if(headers.length>0) builder.headers(headers);
   return client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void anonymousAndMusePermissions() throws Exception {
   var c=HttpClient.newHttpClient();
   assertEquals(401,request(c,"GET","/api/applications",null).statusCode());
   assertEquals(302,request(c,"GET","/",null).statusCode());
   assertEquals(200,request(c,"GET","/healthz",null).statusCode());
   assertEquals(401,request(c,"GET","/api/applications",null,"Authorization","Bearer wrong").statusCode());
   String body="{\"companyName\":\"Fictional Security Test\",\"roleTitle\":\"Intern\",\"status\":\"Saved\"}";
   var create=request(c,"POST","/api/applications",body,"Authorization","Bearer "+key,"Content-Type","application/json");
   assertEquals(201,create.statusCode());
   long id=JsonMapper.builder().build().readTree(create.body()).get("id").asLong();
   assertEquals(200,request(c,"PUT","/api/applications/"+id,body.replace("Saved","Applied"),"Authorization","Bearer "+key,"Content-Type","application/json").statusCode());
   assertEquals(403,request(c,"DELETE","/api/applications/"+id,null,"Authorization","Bearer "+key).statusCode());
   assertEquals(401,request(c,"GET","/api/applications/"+id,null).statusCode());
 }
 @Test void ownerSessionRequiresCsrf() throws Exception {
   var c=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
   var login=request(c,"GET","/login",null);
   var matcher=java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
   assertTrue(matcher.find(), login.statusCode()+" "+login.body());
   String form="username=owner&password=test-owner-password&_csrf="+URLEncoder.encode(matcher.group(1),StandardCharsets.UTF_8);
   assertEquals(302,request(c,"POST","/login",form,"Content-Type","application/x-www-form-urlencoded").statusCode());
   assertEquals(200,request(c,"GET","/api/applications",null).statusCode());
   String body="{\"companyName\":\"Owner Test\",\"roleTitle\":\"Intern\",\"status\":\"Saved\"}";
   assertEquals(403,request(c,"POST","/api/applications",body,"Content-Type","application/json").statusCode());
   var csrf=JsonMapper.builder().build().readTree(request(c,"GET","/api/session",null).body());
   var created=request(c,"POST","/api/applications",body,"Content-Type","application/json",csrf.get("headerName").asText(),csrf.get("token").asText());
   assertEquals(201,created.statusCode());
   long id=JsonMapper.builder().build().readTree(created.body()).get("id").asLong();
   assertEquals(204,request(c,"DELETE","/api/applications/"+id,null,csrf.get("headerName").asText(),csrf.get("token").asText()).statusCode());
 }
}


