package com.example.courseregistration.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ValueObjectsTest {

    @Test
    void learnerRejectsNullValuesAndCopiesCompletedCourses() {
        assertThatNullPointerException().isThrownBy(() -> new Learner(null, Set.of()));
        assertThatNullPointerException().isThrownBy(() -> new Learner("learner", null));

        Learner learner = new Learner("learner", Set.of("intro-java"));

        assertThat(learner.completedCourseIds()).containsExactly("intro-java");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> learner.completedCourseIds().add("databases")
        );
    }

    @Test
    void registrationResultFactoriesProduceExpectedMessages() {
        RegistrationResult ordinaryResult = RegistrationResult.of(RegistrationOutcome.WAITLISTED, "Queued");
        RegistrationResult promotedResult = RegistrationResult.dropped("learner-2");
        RegistrationResult noPromotionResult = RegistrationResult.dropped(null);

        assertThat(ordinaryResult.outcome()).isEqualTo(RegistrationOutcome.WAITLISTED);
        assertThat(ordinaryResult.promotedLearnerId()).isNull();
        assertThat(promotedResult.message()).contains("learner-2");
        assertThat(noPromotionResult.message()).contains("no eligible");
    }
}
