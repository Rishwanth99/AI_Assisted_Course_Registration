package com.example.courseregistration.domain.model;

import com.example.courseregistration.domain.policy.EligibilityPolicy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class CourseOffering {

    private final String offeringId;
    private final int capacity;
    private final Set<String> requiredPrerequisiteCourseIds;
    private final Map<String, Learner> enrolledLearners = new LinkedHashMap<>();
    private final Deque<Learner> waitlistedLearners = new ArrayDeque<>();
    private final ReentrantLock mutationLock = new ReentrantLock();

    public CourseOffering(String offeringId, int capacity, Set<String> requiredPrerequisiteCourseIds) {
        this.offeringId = Objects.requireNonNull(offeringId, "offeringId must not be null");
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.requiredPrerequisiteCourseIds = Set.copyOf(Objects.requireNonNull(
                requiredPrerequisiteCourseIds, "requiredPrerequisiteCourseIds must not be null"));
    }

    public RegistrationResult enroll(Learner learner, EligibilityPolicy eligibilityPolicy) {
        mutationLock.lock();
        try {
            if (enrolledLearners.containsKey(learner.learnerId())) {
                return RegistrationResult.of(RegistrationOutcome.ALREADY_ENROLLED, "Learner is already enrolled.");
            }
            if (isWaitlisted(learner.learnerId())) {
                return RegistrationResult.of(RegistrationOutcome.ALREADY_WAITLISTED, "Learner is already on the waitlist.");
            }
            if (!eligibilityPolicy.isEligible(learner, requiredPrerequisiteCourseIds)) {
                return RegistrationResult.of(RegistrationOutcome.NOT_ELIGIBLE, "Learner does not meet prerequisites.");
            }
            if (enrolledLearners.size() < capacity) {
                enrolledLearners.put(learner.learnerId(), learner);
                return RegistrationResult.of(RegistrationOutcome.ENROLLED, "Learner enrolled successfully.");
            }
            waitlistedLearners.addLast(learner);
            return RegistrationResult.of(RegistrationOutcome.WAITLISTED, "Course is full; learner added to the waitlist.");
        } finally {
            mutationLock.unlock();
        }
    }

    public RegistrationResult drop(String learnerId, EligibilityPolicy eligibilityPolicy) {
        mutationLock.lock();
        try {
            if (enrolledLearners.remove(learnerId) == null) {
                return RegistrationResult.of(RegistrationOutcome.NOT_ENROLLED, "Learner is not enrolled in this offering.");
            }
            Learner promotedLearner = promoteEarliestEligibleLearner(eligibilityPolicy);
            return RegistrationResult.dropped(promotedLearner == null ? null : promotedLearner.learnerId());
        } finally {
            mutationLock.unlock();
        }
    }

    private Learner promoteEarliestEligibleLearner(EligibilityPolicy eligibilityPolicy) {
        for (var iterator = waitlistedLearners.iterator(); iterator.hasNext(); ) {
            Learner candidate = iterator.next();
            if (eligibilityPolicy.isEligible(candidate, requiredPrerequisiteCourseIds)) {
                iterator.remove();
                enrolledLearners.put(candidate.learnerId(), candidate);
                return candidate;
            }
        }
        return null;
    }

    private boolean isWaitlisted(String learnerId) {
        return waitlistedLearners.stream().anyMatch(learner -> learner.learnerId().equals(learnerId));
    }

    public String offeringId() {
        return offeringId;
    }

    public int capacity() {
        return capacity;
    }

    public Set<String> requiredPrerequisiteCourseIds() {
        return requiredPrerequisiteCourseIds;
    }

    public Set<String> enrolledLearnerIds() {
        mutationLock.lock();
        try {
            return Set.copyOf(enrolledLearners.keySet());
        } finally {
            mutationLock.unlock();
        }
    }

    public java.util.List<String> waitlistedLearnerIds() {
        mutationLock.lock();
        try {
            return waitlistedLearners.stream().map(Learner::learnerId).toList();
        } finally {
            mutationLock.unlock();
        }
    }
}
