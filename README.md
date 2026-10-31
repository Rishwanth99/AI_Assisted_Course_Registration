# Course Registration

A bounded Spring Boot service for one course offering's enrollment, dropping, and FIFO waitlist promotion.

## Technology stack

- Java 21
- Spring Boot 3.5
- Spring Web for REST endpoints
- Jakarta Bean Validation for request validation
- Maven for build and dependency management
- JUnit 5, AssertJ, and Spring Boot Test for automated tests
- In-memory `ConcurrentHashMap` repository for the assessment scope

## Prerequisites

- Java 21+
- Maven 3.9+
- Git

Verify the local toolchain:

```bash
java --version
mvn --version
git --version
```

## Clone and set up

```bash
git clone https://github.com/Rishwanth99/AI_Assisted_Course_Registration.git
cd AI_Assisted_Course_Registration
mvn clean test
```

`mvn clean test` downloads project dependencies, compiles the application, and runs the focused automated test suite.

## Run tests

```bash
mvn test
```

## Verify coverage

```bash
mvn verify
```

The build enforces 100% line and branch coverage through JaCoCo. `mvn verify` fails if the coverage threshold is not met. The HTML coverage report is generated at `target/site/jacoco/index.html`.

## Run the application locally

The `dev` profile seeds `java-advanced-2026` with capacity `2` and prerequisite `java-fundamentals`.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application starts on `http://localhost:8080`.

## Try the API

Enroll an eligible learner:

```bash
curl -i -X POST http://localhost:8080/api/v1/course-offerings/java-advanced-2026/enrollments \
  -H 'Content-Type: application/json' \
  -d '{"learnerId":"learner-1","completedCourseIds":["java-fundamentals"]}'
```

View offering state:

```bash
curl http://localhost:8080/api/v1/course-offerings/java-advanced-2026
```

Drop a learner:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/course-offerings/java-advanced-2026/enrollments/learner-1
```

## API endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/course-offerings/{offeringId}/enrollments` | Enroll an eligible learner or add them to the waitlist when full. |
| `DELETE` | `/api/v1/course-offerings/{offeringId}/enrollments/{learnerId}` | Drop an enrolled learner and promote the earliest eligible waitlisted learner. |
| `GET` | `/api/v1/course-offerings/{offeringId}` | View capacity, enrolled learners, and waitlist state. |

See [the design specification](docs/DESIGN.md) for the architecture, rules, flow diagram, and limitations. See [the AI-use record](docs/AI_USE.md) for AI-assisted engineering evidence.
