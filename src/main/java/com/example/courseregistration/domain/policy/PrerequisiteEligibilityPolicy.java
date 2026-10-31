package com.example.courseregistration.domain.policy;

import com.example.courseregistration.domain.model.Learner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PrerequisiteEligibilityPolicy implements EligibilityPolicy {

    @Override
    public boolean isEligible(Learner learner, Set<String> requiredPrerequisiteCourseIds) {
        return learner.completedCourseIds().containsAll(requiredPrerequisiteCourseIds);
    }
}
