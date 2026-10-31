# Design specification

## Service boundary

The service manages enrollment, drop, and waitlist promotion for a single course-offering aggregate. The REST controller is an adapter; `EnrollmentApplicationService` coordinates use cases; `CourseOffering` protects domain invariants; the repository is an infrastructure port.

## Endpoint Flows

### 1. Enroll Learner (`POST /api/v1/course-offerings/{offeringId}/enrollments`)

```mermaid
flowchart TD
    Request["POST Enrollment Request"] --> Lookup{"Offering exists?"}
    Lookup -->|No| NotFound["Return 404 NOT_FOUND"]
    Lookup -->|Yes| Duplicate{"Already enrolled or waitlisted?"}
    Duplicate -->|Yes| Existing["Return 200 OK (ALREADY_ENROLLED / ALREADY_WAITLISTED)"]
    Duplicate -->|No| Eligible{"Meets prerequisites?"}
    Eligible -->|No| Ineligible["Return 422 UNPROCESSABLE_ENTITY (NOT_ELIGIBLE)"]
    Eligible -->|Yes| Seat{"Seat available?"}
    Seat -->|Yes| Enroll["Enroll learner -> Return 201 CREATED (ENROLLED)"]
    Seat -->|No| Waitlist["Add to FIFO waitlist -> Return 202 ACCEPTED (WAITLISTED)"]
```

### 2. Drop Learner (`DELETE /api/v1/course-offerings/{offeringId}/enrollments/{learnerId}`)

```mermaid
flowchart TD
    Request["DELETE Enrollment Request"] --> Lookup{"Offering exists?"}
    Lookup -->|No| NotFound["Return 404 NOT_FOUND"]
    Lookup -->|Yes| Enrolled{"Is learner enrolled?"}
    Enrolled -->|No| NotEnrolled["Return 404 NOT_FOUND (NOT_ENROLLED)"]
    Enrolled -->|Yes| Remove["Remove learner & open seat"]
    Remove --> Waitlist{"Eligible learner in FIFO waitlist?"}
    Waitlist -->|Yes| Promote["Promote candidate -> Return 200 OK (DROPPED + promotedLearnerId)"]
    Waitlist -->|No| KeepOpen["Keep seat open -> Return 200 OK (DROPPED)"]
```

### 3. Get Course Offering (`GET /api/v1/course-offerings/{offeringId}`)

```mermaid
flowchart TD
    Request["GET Course Offering Request"] --> Lookup{"Offering exists?"}
    Lookup -->|No| NotFound["Return 404 NOT_FOUND"]
    Lookup -->|Yes| Snapshot["Read capacity, enrolled learners & waitlist"]
    Snapshot --> Response["Return 200 OK (CourseOfferingResponse)"]
```

The aggregate performs each mutable operation under its `ReentrantLock`, so the state checks and resulting enrollment, queue, or promotion update are atomic for this in-memory implementation.

## API

- `POST /api/v1/course-offerings/{offeringId}/enrollments`
- `DELETE /api/v1/course-offerings/{offeringId}/enrollments/{learnerId}`
- `GET /api/v1/course-offerings/{offeringId}`

The enrollment request contains `learnerId` and `completedCourseIds`. This bounded design treats the caller as the source of learner-completion data; production integration with an identity or academic-record system is out of scope.

## Invariants

- Enrolled learners never exceed offering capacity.
- A learner may appear at most once across enrollment and waitlist collections.
- Only a learner satisfying all required prerequisites may enroll or be promoted.
- Waitlisted learners retain FIFO order, except that currently ineligible learners may be skipped during a promotion attempt.
- A failed or repeated drop does not change offering state.

## Waitlist policy

An eligible learner joins the waitlist only when capacity is full. If, at promotion time, an earlier waitlisted learner is no longer eligible, that learner remains in the waitlist and is skipped. The service continues in FIFO order to promote the earliest eligible learner. This preserves queue position and does not silently discard a learner.

## Concurrency and persistence

`CourseOffering` owns a `ReentrantLock`; enrollment, drop, and promotion run under the same lock. This makes the read-modify-write workflow atomic for the in-memory, single-process repository and prevents capacity oversubscription. The repository uses a `ConcurrentHashMap` for offering lookup.

The in-memory repository is deliberately non-durable. A production replacement would implement `CourseOfferingRepository` using a database transaction plus optimistic locking/versioning or a suitable pessimistic lock. The domain/application interfaces remain unchanged.

## Trade-offs and limitations

- State is reset on restart.
- There is no authentication, authorization, course catalog, persistence, waitlist cancellation, or multi-offering workflow.
- Submitted completion data is trusted for this assessment; a production service would resolve it from an authoritative academic-record service.
- The service uses a per-process lock only; it is not distributed-lock safe.
