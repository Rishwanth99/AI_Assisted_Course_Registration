package com.example.courseregistration.application;

import com.example.courseregistration.domain.model.CourseOffering;
import com.example.courseregistration.domain.model.Learner;
import com.example.courseregistration.domain.model.RegistrationOutcome;
import com.example.courseregistration.domain.policy.PrerequisiteEligibilityPolicy;
import com.example.courseregistration.infrastructure.persistence.InMemoryCourseOfferingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentApplicationServiceTest {

    private static final String OFFERING_ID = "java-advanced-2026";
    private static final Set<String> PREREQUISITES = Set.of("java-fundamentals");

    private InMemoryCourseOfferingRepository courseOfferingRepository;
    private EnrollmentApplicationService enrollmentApplicationService;

    @BeforeEach
    void setUp() {
        courseOfferingRepository = new InMemoryCourseOfferingRepository();
        courseOfferingRepository.save(new CourseOffering(OFFERING_ID, 2, PREREQUISITES));
        enrollmentApplicationService = new EnrollmentApplicationService(
                courseOfferingRepository,
                new PrerequisiteEligibilityPolicy()
        );
    }

    @Test
    void rejectsNullDependenciesAndReportsUnknownOffering() {
        assertThatNullPointerException().isThrownBy(() -> new EnrollmentApplicationService(null, new PrerequisiteEligibilityPolicy()));
        assertThatNullPointerException().isThrownBy(() -> new EnrollmentApplicationService(courseOfferingRepository, null));

        assertThatThrownBy(() -> enrollmentApplicationService.getCourseOffering("unknown"))
                .isInstanceOf(CourseOfferingNotFoundException.class)
                .hasMessage("Course offering 'unknown' was not found.");
    }

    @Test
    void enrollsEligibleLearnerWhenCapacityIsAvailable() {
        var result = enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));

        assertThat(result.outcome()).isEqualTo(RegistrationOutcome.ENROLLED);
        assertThat(courseOffering().enrolledLearnerIds()).containsExactly("learner-1");
    }

    @Test
    void doesNotDuplicateAnExistingEnrollment() {
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));

        var duplicateRequest = enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));

        assertThat(duplicateRequest.outcome()).isEqualTo(RegistrationOutcome.ALREADY_ENROLLED);
        assertThat(courseOffering().enrolledLearnerIds()).containsExactly("learner-1");
    }

    @Test
    void addsEligibleLearnerToWaitlistWhenOfferingIsFullAndDoesNotDuplicateRequest() {
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-2"));

        var firstRequest = enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-3"));
        var duplicateRequest = enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-3"));

        assertThat(firstRequest.outcome()).isEqualTo(RegistrationOutcome.WAITLISTED);
        assertThat(duplicateRequest.outcome()).isEqualTo(RegistrationOutcome.ALREADY_WAITLISTED);
        assertThat(courseOffering().waitlistedLearnerIds()).containsExactly("learner-3");
    }

    @Test
    void promotesEarliestEligibleWaitlistedLearnerAfterDrop() {
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-2"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-3"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-4"));

        var result = enrollmentApplicationService.drop(OFFERING_ID, "learner-1");

        assertThat(result.outcome()).isEqualTo(RegistrationOutcome.DROPPED);
        assertThat(result.promotedLearnerId()).isEqualTo("learner-3");
        assertThat(courseOffering().enrolledLearnerIds()).containsExactlyInAnyOrder("learner-2", "learner-3");
        assertThat(courseOffering().waitlistedLearnerIds()).containsExactly("learner-4");
    }

    @Test
    void rejectsLearnerWhoDoesNotMeetPrerequisites() {
        var result = enrollmentApplicationService.enroll(OFFERING_ID, new Learner("learner-1", Set.of()));

        assertThat(result.outcome()).isEqualTo(RegistrationOutcome.NOT_ELIGIBLE);
        assertThat(courseOffering().enrolledLearnerIds()).isEmpty();
        assertThat(courseOffering().waitlistedLearnerIds()).isEmpty();
    }

    @Test
    void skipsAndRetainsNowIneligibleLearnerDuringPromotion() {
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-2"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-3"));
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-4"));

        // A policy change simulates learner-3 becoming ineligible after waitlisting.
        EnrollmentApplicationService serviceWithChangedPolicy = new EnrollmentApplicationService(
                courseOfferingRepository,
                (learner, prerequisites) -> !learner.learnerId().equals("learner-3")
        );
        var result = serviceWithChangedPolicy.drop(OFFERING_ID, "learner-1");

        assertThat(result.promotedLearnerId()).isEqualTo("learner-4");
        assertThat(courseOffering().waitlistedLearnerIds()).containsExactly("learner-3");
    }

    @Test
    void reportsRepeatedAndInvalidDropsWithoutChangingState() {
        enrollmentApplicationService.enroll(OFFERING_ID, eligibleLearner("learner-1"));

        var firstDrop = enrollmentApplicationService.drop(OFFERING_ID, "learner-1");
        var repeatedDrop = enrollmentApplicationService.drop(OFFERING_ID, "learner-1");
        var unknownLearnerDrop = enrollmentApplicationService.drop(OFFERING_ID, "unknown");

        assertThat(firstDrop.outcome()).isEqualTo(RegistrationOutcome.DROPPED);
        assertThat(repeatedDrop.outcome()).isEqualTo(RegistrationOutcome.NOT_ENROLLED);
        assertThat(unknownLearnerDrop.outcome()).isEqualTo(RegistrationOutcome.NOT_ENROLLED);
        assertThat(courseOffering().enrolledLearnerIds()).isEmpty();
    }

    @Test
    void neverExceedsCapacityWhenEnrollmentRequestsArriveConcurrently() throws Exception {
        int requestCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<RegistrationOutcome> outcomes = new ArrayList<>();

        try {
            for (int index = 0; index < requestCount; index++) {
                String learnerId = "learner-" + index;
                executorService.submit(() -> {
                    ready.countDown();
                    start.await();
                    RegistrationOutcome outcome = enrollmentApplicationService
                            .enroll(OFFERING_ID, eligibleLearner(learnerId))
                            .outcome();
                    synchronized (outcomes) {
                        outcomes.add(outcome);
                    }
                    return null;
                });
            }
            assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
            start.countDown();
        } finally {
            executorService.shutdown();
            assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(courseOffering().enrolledLearnerIds()).hasSize(2);
        assertThat(courseOffering().waitlistedLearnerIds()).hasSize(18);
        assertThat(outcomes).containsOnly(RegistrationOutcome.ENROLLED, RegistrationOutcome.WAITLISTED);
    }

    private CourseOffering courseOffering() {
        return courseOfferingRepository.findByOfferingId(OFFERING_ID).orElseThrow();
    }

    private Learner eligibleLearner(String learnerId) {
        return new Learner(learnerId, Set.of("java-fundamentals"));
    }
}
