# AI-assisted engineering record

## Purpose and assessment evidence

This is a factual record of how AI assisted this bounded assessment. It records the prompts that informed the work, relevant guidance received, my own review and decisions, affected files, and verification evidence. It does not claim AI output was accepted without independent review.

## Activity 1 — Assessment analysis and planning

### Prompt used

> I have an AI assessment to complete and have attached the assessment brief. Please analyse the problem in detail, review the acceptance criteria, create an implementation plan, recommend suitable design patterns, state the assumptions and their rationale, and identify any input needed before implementation begins.

### Relevant AI response summary

The response identified a bounded, single-course-offering feature. It mapped the acceptance criteria to enrollment, dropping, FIFO waitlisting, promotion, prerequisites, duplicate protection, focused tests, design documentation, setup instructions, and an AI-use record. It recommended a service boundary, repository abstraction, eligibility-policy abstraction, explicit outcomes, and a FIFO queue.

### My review and decision

- **Accepted:** Keep the work bounded to one offering; do not add a frontend, database, authentication, catalog, payments, or multi-term workflows.
- **Accepted:** Document prerequisite and waitlist assumptions explicitly.
- **Edited:** Changed the suggested technology direction to Java and Spring Boot because it is my primary development stack.
- **Accepted:** Use focused tests rather than unnecessary infrastructure.

### Files and decisions informed

- `docs/DESIGN.md`
- `CourseOffering`
- `EnrollmentApplicationService`
- `EnrollmentApplicationServiceTest`

### Verification evidence

- Reviewed the implementation and tests against each assessment acceptance criterion.
- Compiled the framework-independent domain core using `javac --release 21`.
- Full Maven verification will be recorded only after `mvn test` is successfully executed.

## Activity 2 — Java/Spring architecture and code quality

### Prompt used

> I am a Java developer and want to use Java and Spring. Please implement the solution to production-quality standards, following SOLID principles and Java coding best practices, and provide a revised implementation plan.

### Relevant AI response summary

The response proposed Java 21, Spring Boot, REST endpoints, Bean Validation, JUnit, a layered architecture, an application service, domain aggregate, repository port with in-memory adapter, eligibility-policy abstraction, consistent API errors, and a focused concurrency test.

### My review and decision

- **Accepted:** Controller → application service → domain model → repository adapter architecture.
- **Accepted:** Dependency inversion through `CourseOfferingRepository` and `EligibilityPolicy` interfaces.
- **Accepted:** An in-memory repository because no starter project or database was supplied.
- **Edited:** Restrict `CommandLineRunner` to the `dev` profile so seed data does not run by default.
- **Rejected:** A database, frontend, identity integration, and configurable policy engine because they exceed scope.

### Files and decisions informed

- `pom.xml`
- `src/main/java/com/example/courseregistration/api/`
- `src/main/java/com/example/courseregistration/application/`
- `src/main/java/com/example/courseregistration/domain/`
- `src/main/java/com/example/courseregistration/infrastructure/`

### Verification evidence

- Manually reviewed package separation: HTTP in `api`, orchestration in `application`, business rules in `domain`, and storage/bootstrap in `infrastructure`.
- Confirmed `CourseOffering`, rather than the controller, owns all enrollment and queue mutation rules.

## Activity 3 — Eligibility, waitlist policy, API, and naming

### Prompts used

> Please review the eligibility policy again and explain how it should work. I also want a REST API and a `CommandLineRunner` so that I can test the application accordingly.

> Please also follow standard Java naming conventions throughout the implementation.

### Relevant AI response summary

The response described the policy for a learner who becomes ineligible while waiting: scan the queue in FIFO order, skip but retain that learner, and promote the earliest eligible learner. It also recommended REST endpoints, a development-only `CommandLineRunner`, and standard Java naming conventions.

### My review and decision

- **Accepted:** A learner must satisfy every configured prerequisite to enroll.
- **Accepted:** An ineligible learner is neither enrolled nor added to the waitlist.
- **Accepted:** Retain but skip a currently ineligible waitlisted learner at promotion time, then continue FIFO scanning for the next eligible learner.
- **Accepted:** Use REST for API testing and a `dev`-profile `CommandLineRunner` only for sample data.
- **Accepted:** Lower-case packages, PascalCase types, camelCase members, descriptive names, and constants only where appropriate.

### Files and decisions informed

- `EligibilityPolicy`
- `PrerequisiteEligibilityPolicy`
- `CourseOffering`
- `DevelopmentDataInitializer`
- `docs/DESIGN.md`

### Verification evidence

- `skipsAndRetainsNowIneligibleLearnerDuringPromotion` tests the documented policy.
- `promotesEarliestEligibleWaitlistedLearnerAfterDrop` tests FIFO promotion.
- Reviewed source names and package names against Java naming conventions.

## Activity 4 — Documentation improvements

### Prompt used

> Please enrich the design documentation with a clear flow diagram. Also update the README to specify the technology stack and provide clear instructions for cloning and running the application.

### Relevant AI response summary

The response recommended a Mermaid flow diagram that shows enrollment, duplicate checks, prerequisite validation, capacity handling, FIFO waitlisting, dropping, and promotion. It also recommended documenting the technology stack, prerequisites, cloning, test execution, application startup, and REST API usage in the README.

### My review and decision

- **Accepted:** Add an enrollment and drop flow diagram to the design specification.
- **Accepted:** Add technology-stack, clone, setup, test, startup, and API instructions to the README.
- **Accepted:** Use the configured Git remote as the clone URL.

### Files and decisions informed

- `docs/DESIGN.md`
- `README.md`

### Verification evidence

- Reviewed the documentation steps against the Maven commands and REST endpoints provided by the application.

## Activity 5 — Comprehensive test coverage

### Prompt used

> Please increase test coverage to 100% and include all relevant edge cases and negative test scenarios.

### Relevant AI response summary

The response recommended enforcing line and branch coverage with JaCoCo, strengthening the concurrent-enrollment test, and adding focused domain, repository, application, controller, bootstrap, and response-mapping tests for normal, edge, and failure behavior.

### My review and decision

- **Accepted:** Enforce 100% line and branch coverage with the Maven build.
- **Accepted:** Add tests for validation failures, missing offerings, duplicate requests, invalid constructor input, no-promotion outcomes, all HTTP status mappings, repository behavior, and development bootstrap behavior.
- **Accepted:** Correct the concurrency test so enrollment operations execute concurrently instead of being serialized by test code.

### Files and decisions informed

- `pom.xml`
- `src/test/java/`

### Verification evidence

- Command: `mvn verify`
- Result: `BUILD SUCCESS`; 28 tests run with 0 failures and 0 errors.
- JaCoCo result: 100% line coverage (138/138 lines) and 100% branch coverage (25/25 branches).

## Independent review and verification checklist

- [x] The aggregate owns capacity, enrollment, drop, and waitlist mutation rules.
- [x] Enrollment is idempotent for enrolled and waitlisted learners.
- [x] `ReentrantLock` makes a drop and its promotion one in-memory critical section.
- [x] Tests cover capacity, full capacity, duplicate requests, FIFO promotion, prerequisite failure, repeated/invalid drops, the selected ineligible-learner policy, concurrent enrollment, invalid API requests, successful API enrollment, ineligible API enrollment, and unknown offerings.
- [x] `mvn verify` completed successfully with 28 tests, 0 failures, and 0 errors (31 October 2026).
- [x] JaCoCo enforces 100% line coverage (138/138) and 100% branch coverage (25/25).

## Limitations of AI assistance

- AI was used for analysis, architecture suggestions, and implementation assistance; all design choices were reviewed and explicitly selected for this assessment.
- The final submission remains limited to one in-memory course offering. Distributed locking, durable persistence, identity integration, and real academic-record lookup are intentionally out of scope.
