# Internship Application Tracker

A beginner-friendly application for recording internship opportunities, updating progress, and keeping notes. The browser frontend and REST API are served by one Spring Boot application.

## Tech stack

- Java 17 or later (verified locally with Java 26)
- Spring Boot 4.1.1 and Spring Web MVC
- Spring Data JPA, Hibernate, and H2
- Jakarta Bean Validation
- Plain HTML, CSS, and JavaScript
- Maven; JUnit and Spring Boot Test for integration tests

## Main features

- Add, list, view by ID, edit, and delete applications.
- Track Saved, Applied, Interview, Offer, and Rejected statuses.
- Filter by status and search company or role, case-insensitively; combine both.
- Store optional application dates and notes.
- Validate required company, role, and status. Company and role allow 120 characters each; notes allow 2,000.
- Return useful 400 validation/input errors and 404 errors for missing records.
- Persist data in a local H2 file across restarts.

## How to run

Install a JDK (Java 17+) and Maven 3.6.3+. Set `JAVA_HOME` to your JDK folder, and make Maven available on your PATH. Run commands from this folder so the database stays in the same location.

```sh
mvn clean verify
mvn spring-boot:run
```

Open http://localhost:8080. Stop with Ctrl+C. The app listens on the local computer only.

Alternatively, after building:

```sh
java -jar target/internship-application-tracker-0.0.1-SNAPSHOT.jar
```

For this workspace, Maven was downloaded into the sibling `.tools` folder. In PowerShell you can use it without installing Maven globally:

```powershell
..\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=../.tools/repository' clean verify
..\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=../.tools/repository' spring-boot:run
```

The first build needs internet access to download dependencies. If port 8080 is occupied, run the JAR with `--server.port=8081`. The database is at `data/internships.mv.db`; tests use an isolated in-memory H2 database and do not touch it.

## Example API endpoints

| Method | Endpoint | Behavior |
| --- | --- | --- |
| POST | `/api/applications` | Create; returns 201 and a Location header |
| GET | `/api/applications` | List, newest IDs first |
| GET | `/api/applications/1` | Get one; returns 200 or 404 |
| PUT | `/api/applications/1` | Replace editable fields; returns 200 or 404 |
| DELETE | `/api/applications/1` | Delete; returns 204 or 404 |
| GET | `/api/applications?status=Interview` | Filter by exact status |
| GET | `/api/applications?search=java` | Search company or role |
| GET | `/api/applications?status=Applied&search=java` | Combine filter and search |

POST and PUT accept `Content-Type: application/json` with this body:

```json
{
  "companyName": "Example Co",
  "roleTitle": "Backend Intern",
  "status": "Applied",
  "dateApplied": "2026-10-01",
  "notes": "Follow up next week"
}
```

`dateApplied` and `notes` are optional; dates use YYYY-MM-DD. Status values are case-sensitive. POST ignores a supplied ID; PUT uses the URL ID. PUT is a full update, so send all required fields; omitted optional fields become null.

PowerShell example:

```powershell
$body = @{companyName='Example Co'; roleTitle='Backend Intern'; status='Saved'} | ConvertTo-Json
Invoke-RestMethod http://localhost:8080/api/applications -Method Post -ContentType 'application/json' -Body $body
Invoke-RestMethod 'http://localhost:8080/api/applications?search=example'
```

Validation error example (400):

```json
{"message":"Please check the application fields","errors":{"companyName":"Company name is required"}}
```

## Project structure

```text
src/main/java/com/example/internshiptracker/
  InternshipTrackerApplication.java   starts Spring Boot
  controller/                        HTTP endpoints and error handling
  service/                           application operations and transactions
  repository/                        JPA database access and search query
  model/                             entity and status enum
src/main/resources/
  application.properties             H2 and server settings
  static/                            index.html, styles.css, app.js
src/test/java/                        HTTP integration tests
README.md
PROJECT_WALKTHROUGH.md
CAREER_MATERIALS.md
```

## Known limitations

- This is a local learning project with no login or per-user data separation.
- No pagination: listing loads all matching records.
- No duplicate detection, reminders, attachments, or deployment configuration.
- Concurrent edits use last-write-wins; there is no version conflict check.
- H2 is a small local database; `ddl-auto=update` is convenient here but production schema changes need deliberate migrations.
- The entity doubles as the request/response model to keep the code small; larger APIs often introduce DTOs.
- Date is optional even for non-Saved statuses; there are no status-transition or date-order rules.

## What I learned

Use these as learning targets; claim them personally only after reviewing and practicing the code:

- Trace a browser JSON request through controller, service, repository, and database.
- Map a Java entity to a table and let Hibernate generate SQL.
- Use GET, POST, PUT, and DELETE with meaningful HTTP responses.
- Validate inputs on the server as well as in the browser.
- Use constructor injection and transactions to organize backend code.
- Test REST behavior against a running application and isolated database.

Read [PROJECT_WALKTHROUGH.md](PROJECT_WALKTHROUGH.md) for the flow and [CAREER_MATERIALS.md](CAREER_MATERIALS.md) for interview preparation and honest AI attribution.

## Verification performed

- Maven `clean verify`: BUILD SUCCESS; six HTTP integration tests passed, with no failures or errors.
- Packaged JAR started successfully using the normal file-backed H2 configuration.
- Browser checks confirmed add, edit, search, status filtering, and the empty state.
- A saved record remained after stopping and restarting the packaged application; the temporary verification record was then removed through the REST API.
- The browser automation connection stalled when handling the delete confirmation. DELETE itself was verified by the automated API test and cleanup request; the browser confirmation interaction was not fully verified.

If using the app instance started during verification on this computer, stop it before starting a second instance:

```powershell
Stop-Process -Id ([int](Get-Content runtime.pid))
```

Only use that command for the current verification instance. Normal foreground runs can be stopped with Ctrl+C.

## Public Render demo

Deploy this repository using the included render.yaml Blueprint (free Docker web service).
The demo profile seeds five fictional applications into an in-memory H2 database.
All existing CRUD, search, filtering and validation features remain available.
Visitors share the same demo records; records reset on every restart. Do not use real personal data.
The Docker build runs the API test suite before packaging. Local defaults retain the original file database and loopback binding.

## Private deployment

Activate ONLY the `private` Spring profile. Required server environment variables:
- TRACKER_PASSWORD_HASH: `{bcrypt}` plus an encoded owner password. Username is `owner`.
- MUSE_TOKEN_SHA256: hexadecimal SHA-256 of a randomly generated 32-byte or stronger access token.
- TRACKER_DATABASE_URL: PostgreSQL JDBC URL using TLS (`sslmode=require`).
- TRACKER_DATABASE_USER and TRACKER_DATABASE_PASSWORD: database credentials.

Set Render health check to `/healthz`. Browser authentication uses a secure HttpOnly SameSite session cookie and CSRF protection. The API accepts Muse's `Authorization: Bearer TOKEN` for read/create/update; DELETE is prohibited for Muse. Change the stored token hash and redeploy to revoke an old token. Never commit credentials or actual application records.

Use a database with the public Data API disabled. If enabling it later, restrict grants and enable RLS before exposing any tables. Import real records only after anonymous access returns 401 and persistent database connections are verified.
