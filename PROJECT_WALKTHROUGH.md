# Project walkthrough

## When a user clicks Add Application

1. The browser checks the required fields. JavaScript reads the form, trims company and role, and converts an empty date to null.
2. `fetch` sends JSON using POST to `/api/applications` on the same server.
3. Spring MVC selects the controller method. Its JSON converter creates an `InternshipApplication` Java object from the body. `@Valid` checks the field constraints.
4. The controller calls `service.create(input)`. The service clears any client-supplied ID, trims names, and calls `repository.save` inside a transaction.
5. Spring Data JPA implements the repository. Hibernate maps the object to a row and sends SQL to H2. H2 generates the ID and stores the record in the local database file.
6. The transaction commits. The controller returns 201 Created, a Location header, and JSON for the saved object.
7. JavaScript resets the form and sends GET to refresh the list using the current filters. It shows a message if the new record is hidden by those filters.

The path is **frontend -> REST endpoint -> controller -> service -> repository -> database**, and the response returns to the browser. A REST endpoint is an HTTP method plus a URL; it is not a separate Java layer.

## What each part does

### Controller

`InternshipApplicationController` handles HTTP: URLs, path IDs, query parameters, request bodies, validation, and response codes. It delegates application operations to the service. `ApiExceptionHandler` turns missing records into 404 and invalid inputs into 400 JSON messages.

### Service

`InternshipApplicationService` coordinates create, update, delete, and lookup. It decides that missing IDs are errors, ensures IDs come from the database, and trims names. `@Transactional` groups a write operation into a unit that commits on success or rolls back on a runtime failure. Read methods are marked read-only. A transaction is useful even though the current operations are simple.

### Repository

`InternshipApplicationRepository` extends `JpaRepository<InternshipApplication, Long>`. Spring creates its implementation at startup, providing `save`, `findById`, and `delete`. The custom JPQL query combines an optional status filter with literal, case-insensitive company/role search. JPQL uses Java entity fields instead of SQL table names. An empty search matches all names; results are ordered by ID descending.

### JPA and Hibernate

JPA is a specification for mapping Java objects to relational data. Hibernate implements it and is supplied by the JPA starter. `@Entity` marks the mapped class, `@Id` identifies its primary key, and `@GeneratedValue` delegates ID generation to the database. `@Enumerated(EnumType.STRING)` stores statuses as readable names instead of numeric positions. Hibernate generates SQL, tracks managed object changes, and reads rows into objects. It does not replace the database: H2 actually stores the data.

### Model and database

One `InternshipApplication` maps to one row with ID, company, role, status, date, and notes. Company, role, and status are required. The database configuration uses a file under `data/` for persistence. Tests override that URL with an in-memory database. `ddl-auto=update` lets Hibernate adjust the schema at startup for this learning version.

## CRUD explained

| Operation | Browser action / HTTP | Backend behavior |
| --- | --- | --- |
| Create | Add Application / POST | Validate, clear ID, save new entity, return 201 |
| Read | Load or search / GET | Query matching rows and return JSON; a single missing ID returns 404 |
| Update | Edit, then Save Changes / PUT | Find existing row, copy editable fields, save in a transaction, return 200 |
| Delete | Delete and confirm / DELETE | Find and delete row, return 204 without a body |

An update changes an existing record; it does not create a new one when the ID is missing. The UI loads the current row into the form for editing. The URL ID is authoritative. Delete asks for confirmation because it removes the stored record.

## Validation and errors

HTML validation helps the user, but callers can bypass the browser, so the server also checks inputs. `@NotBlank` rejects empty or whitespace-only names, `@NotNull` requires a status, and `@Size` limits lengths. Invalid status names, malformed JSON, invalid dates, and nonnumeric IDs return 400. Field errors include an `errors` map. JavaScript displays returned messages and uses `textContent` when rendering input so notes are displayed as text.

## Five common interview questions

1. **Why split the code into layers?** The controller handles HTTP, the service coordinates operations, and the repository handles persistence. Each class has a clear purpose, so changes are easier to follow.
2. **What is the difference between JPA and Hibernate?** JPA defines the persistence interfaces and mapping rules. Hibernate implements them and generates the SQL used with H2.
3. **How does updating differ from creating?** Creating clears the ID and saves a new row. Updating finds the URL ID first and copies fields onto the existing entity. A missing ID produces 404.
4. **Why validate on the backend?** Anyone can call the REST API without using the form. Backend validation protects the data regardless of the client.
5. **What would you improve next?** I would add pagination and more tests as data grows. If moving beyond a local demo, I would plan authentication, a production database, and schema migrations; those are not implemented here.

## Practice before an interview

Add a record, inspect the browser Network tab, follow the controller call through the service, and locate the repository query. Change one validation limit and rerun the tests. Explain what happens for an unknown ID and why tests use a separate database. Understanding those steps matters more than memorizing annotations.
