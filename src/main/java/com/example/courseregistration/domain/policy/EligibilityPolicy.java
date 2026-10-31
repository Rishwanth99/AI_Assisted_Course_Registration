package com.example.courseregistration.domain.policy;

import com.example.courseregistration.domain.model.Learner;

import java.util.Set;

public interface EligibilityPolicy {

    boolean isEligible(Learner learner, Set<String> requiredPrerequisiteCourseIds);
}
