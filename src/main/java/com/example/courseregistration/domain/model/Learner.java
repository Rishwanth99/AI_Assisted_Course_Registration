package com.example.courseregistration.domain.model;

import java.util.Objects;
import java.util.Set;

public record Learner(String learnerId, Set<String> completedCourseIds) {

    public Learner {
        Objects.requireNonNull(learnerId, "learnerId must not be null");
        completedCourseIds = Set.copyOf(Objects.requireNonNull(completedCourseIds, "completedCourseIds must not be null"));
    }
}
