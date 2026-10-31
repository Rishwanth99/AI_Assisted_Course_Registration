# Course Registration

A bounded Spring Boot service for one course offering's enrollment, dropping, and FIFO waitlist promotion.

## Requirements

- Java 21+
- Maven 3.9+

## Run tests

```bash
mvn test
```

## Run locally with sample data

The `dev` profile seeds `java-advanced-2026` with capacity `2` and prerequisite `java-fundamentals`.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

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

See [the design specification](docs/DESIGN.md) for behavior and constraints.
