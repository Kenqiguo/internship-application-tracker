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
- Track Saved, Applied, Online Assessment (OA), Interview, Recruiter Reachout, Offer, and Rejected statuses.
- Optional details per application: location, job URL, requisition ID, resume version, source, visa/work-authorization notes, and confirmation date.
- Older databases are upgraded automatically on startup (the status column changes from an H2 ENUM to text so new statuses can be stored; existing data is kept).
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
| GET | `/api/applications/lookup?requisitionId=R-1` | Find possible duplicates by `externalId`, `requisitionId`, `jobUrl`, or exact `company` + `role` |

PUT replaces the original five fields. The optional detail fields keep their stored value when a request omits them (send an empty string to clear one), so older clients never erase details.
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

## Live deployment

| Site | URL | Access |
|---|---|---|
| Private tracker | https://internship-tracker-private.onrender.com | Owner login only (username `owner`). Real data in Neon PostgreSQL. |
| Public demo | not deployed yet (optional) | Can be created from the `internship-tracker-demo` entry in `render.yaml`. |

### Architecture

One repository, two free Render web services defined in `render.yaml`, both built from the same `Dockerfile`
(the build runs the full test suite before packaging):

- **internship-tracker-demo** runs the `demo` profile: in-memory H2, fictional seed data, capped at 100 records,
  no login. It has **no database credentials**, so it cannot read or change private records.
- **internship-tracker-private** runs the `private` profile: PostgreSQL on Neon (free tier) in a private `tracker`
  schema over TLS, owner form login with a secure HttpOnly SameSite session cookie, CSRF protection, and an
  automation bearer token that can read/create/update but never delete. Anonymous API calls get `401`.

### Configuration (private service, set in the Render dashboard, never committed)

| Variable | Value |
|---|---|
| `TRACKER_DATABASE_URL` | `jdbc:postgresql://<neon-host>/<database>?sslmode=require` |
| `TRACKER_DATABASE_USER` / `TRACKER_DATABASE_PASSWORD` | Neon role and password |
| `TRACKER_PASSWORD_HASH` | `{bcrypt}` hash of the owner password |
| `MUSE_TOKEN_SHA256` | SHA-256 hex of the automation token |

Prepare them locally in the job-auto-apply project: `python scripts/tracker_cloud.py render-env` (database
settings + automation-token hash) and `python scripts/tracker_cloud.py set-password` (you type the owner password
twice; only its bcrypt hash is saved). Confirm with `check-password`. The automation token is stored in Windows
Credential Manager; only its hash goes to Render.

### Costs (as of November 2026)

| Item | Plan | Cost |
|---|---|---|
| Render web service | Free: 750 instance-hours/month shared by the workspace; sleeps after 15 min idle, ~1 min to wake | $0 |
| Neon PostgreSQL | Free: 1 GB storage, 100 CU-hours/month, suspends after 5 min idle | $0 |

Upgrade paths if needed: Render Starter web service (always on, about $7/month) or Neon Launch plan.

### Backups and retention

- **Neon point-in-time restore**: free plan keeps a 6-hour history (up to 1 GB of changes). Restore from the Neon
  console: Branches > Restore.
- **Daily JSON backup** (job-auto-apply project): `python scripts/tracker_cloud.py backup` saves every cloud record to
  `internship-application-tracker-runtime\backups\cloud\` and keeps the newest 90 files.
- **Full dump** (any time): `pg_dump "postgresql://USER:PASSWORD@HOST/DB?sslmode=require" -n tracker > tracker.sql`.
- The local H2 tracker on this computer is untouched and remains a separate copy.
- Restore from a JSON backup by re-running the import against an empty database (records are POSTed, never deleted).

### Maintenance

- **Deploys**: the private service was created through the Render API from the public repo URL, so pushes do
  NOT auto-deploy. After pushing to `main`, run `python scripts/tracker_cloud.py render-redeploy` (job-auto-apply
  project) or click Manual Deploy > Deploy latest commit in Render. A failing test stops the Docker build, and a
  version that fails its health check is not switched in, so the running site stays up.
- **Change the owner password**: run `tracker_cloud.py set-password`, paste the new `TRACKER_PASSWORD_HASH` in
  Render > internship-tracker-private > Environment, then Save (redeploys).
- **Revoke the automation token**: same as above; the old token stops working after the redeploy.
- **Check health**: `python scripts/tracker_cloud.py verify`, or open `/healthz` on either service.
- **Logs**: Render dashboard > service > Logs. Database usage: Neon console > Monitoring.
- **Schema**: Hibernate `ddl-auto=update` adds new columns automatically; it never drops data.
- **Free-tier watch**: Neon emails when you approach limits; Render suspends free services if the 750 hours run out
  (unlikely, because idle services sleep).

### Automation (Muse / job-auto-apply) integration

- After an application is confirmed submitted, job-auto-apply calls `POST /api/applications` (or `PUT` if it already
  exists) with company, role, date applied, status, location, job URL, requisition ID, resume version, source,
  visa notes, and notes. Confirmation / OA / interview / rejection emails later update the status.
- Authentication: `Authorization: Bearer <token>`. Only the token's SHA-256 is stored on Render; the token itself is
  in Windows Credential Manager on the job-auto-apply computer. It can read/create/update, never delete.
- Duplicates: each record carries `externalId = jobauto-<sha256 of the official job URL>`; before creating, the
  client checks `/api/applications/lookup` by externalId, requisition ID (same company only), job URL, and
  company + role, and updates the match instead of inserting.
- If the tracker is asleep or down, updates wait in job-auto-apply's local queue and are retried every 10 minutes.
