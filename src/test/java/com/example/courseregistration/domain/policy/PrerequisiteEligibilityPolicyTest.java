package com.example.courseregistration.domain.policy;

import com.example.courseregistration.domain.model.Learner;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PrerequisiteEligibilityPolicyTest {

    private final PrerequisiteEligibilityPolicy policy = new PrerequisiteEligibilityPolicy();

    @Test
    void returnsTrueWhenAllPrerequisitesAreCompleted() {
        assertThat(policy.isEligible(new Learner("learner", Set.of("intro-java", "databases")), Set.of("intro-java")))
                .isTrue();
    }

    @Test
    void returnsFalseWhenAnyPrerequisiteIsMissing() {
        assertThat(policy.isEligible(new Learner("learner", Set.of("intro-java")), Set.of("intro-java", "databases")))
                .isFalse();
    }
}
