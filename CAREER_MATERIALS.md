# Career materials

Use these drafts after you have reviewed, tested, modified, and understood the project. The implementation was assisted by AI/Codex; do not imply independent authorship or learning you have not yet done.

## 1. Two resume bullets

- Developed an AI-assisted internship tracker using Java, Spring Boot, and a REST API for CRUD operations, status filtering, and company/role search.
- Used Spring Data JPA/Hibernate with H2 database persistence, server-side validation, HTTP error handling, and automated API integration tests.

## 2. 30-second interview explanation

“This is a local internship application tracker that helps me organize companies, roles, application statuses, dates, and notes. I chose it as a small project for learning backend development. It uses Java and Spring Boot for a REST API, Spring Data JPA and Hibernate with H2 for persistence, and plain HTML, CSS, and JavaScript for the frontend. It supports adding, viewing, editing, deleting, searching, and filtering applications. Codex helped implement and debug it.”

## 3. 60-second technical explanation

“When I submit the form, JavaScript sends a JSON POST request to a REST endpoint. Spring MVC routes it to the controller and converts the JSON into a Java object. The controller uses validation to check the required fields, then calls the service. The service clears any supplied ID, trims the names, and coordinates saving within a transaction. It calls the Spring Data JPA repository, whose implementation is provided by Spring. Hibernate implements JPA: it maps the entity to a table and generates SQL. H2 stores the row in a local database file and generates the ID. The controller returns 201 Created with the saved record, and the frontend reloads the list. Updates find the existing ID before changing fields; deletes return 204. Invalid input returns 400, while missing records return 404. Search and status filtering share one repository query.”

## 4. Five likely interview questions

1. **Why use Spring Boot?** It configures the web server, MVC, and persistence integrations from the dependencies and properties, so I can focus on a small application's behavior.
2. **What does the service add?** It coordinates application operations and transactions, trims names, and checks that a record exists before update or delete. It keeps those decisions out of HTTP handlers.
3. **What does the repository do?** It supplies database operations through Spring Data JPA. I extend `JpaRepository` and define one JPQL query for search and status filtering.
4. **Does the data survive a restart?** Yes, the normal application uses a file-based H2 database, provided I run it from the same folder. Tests use a separate in-memory database.
5. **What did AI do, and what did you do?** Codex helped implement and debug the project. My contribution should be described based on the code I actually reviewed, tests I ran, changes I made, and behavior I can explain. I would show one concrete change rather than claim I wrote every line independently.

## 5. What I should be able to explain before putting this on my resume

| Concept | What I should explain |
| --- | --- |
| CRUD | Create, Read, Update, Delete and how each changes or retrieves records |
| REST API | Method-and-URL endpoints that exchange JSON and meaningful HTTP status codes |
| Spring Boot | Application startup, dependency-based configuration, and the embedded server |
| Controller | Maps HTTP input to Java calls and returns responses |
| Service | Coordinates application operations and transaction boundaries |
| Repository | Provides persistence methods and the search query |
| JPA | The specification and annotations for object-to-relational mapping |
| Hibernate | The JPA implementation that maps objects and executes generated SQL |
| H2 database | The actual relational database; file-backed for the app, in-memory for tests |
| GET | Reads the list or one application |
| POST | Creates an application; returns 201 with its generated ID |
| PUT | Updates all editable fields for an existing URL ID |
| DELETE | Removes an existing application; returns 204 |

Also be able to explain constructor injection, why the browser's validation is insufficient, why a missing record returns 404, and which limitations remain. Run the tests yourself and make a small change you can explain.

## 6. How to describe AI assistance honestly

If you have completed these activities, you can say: “AI/Codex helped with implementation and debugging. I reviewed the code, ran and inspected the tests, modified it, and learned how the request travels through each layer.” Use that statement only when it accurately describes what you did. If you have not yet reviewed or modified the code, say: “Codex generated the initial implementation, and I am currently studying and testing it.”

Name a real example of a change you made and explain its effect. Do not claim independent authorship, production deployment, users, performance improvements, or metrics that you cannot substantiate. The strongest evidence is being able to trace a request and confidently change the code.

## 7. Short portfolio description

An AI-assisted Java Spring Boot project for tracking internship applications with a simple HTML/CSS/JavaScript frontend. It provides a REST CRUD API, status filtering, company/role search, validation, and local H2 persistence through Spring Data JPA/Hibernate. Automated API integration tests and a beginner-friendly walkthrough document how the application works.
