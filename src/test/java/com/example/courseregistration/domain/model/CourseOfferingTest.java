package com.example.courseregistration.domain.model;

import com.example.courseregistration.domain.policy.PrerequisiteEligibilityPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CourseOfferingTest {

    private final PrerequisiteEligibilityPolicy eligibilityPolicy = new PrerequisiteEligibilityPolicy();

    @Test
    void rejectsInvalidAggregateConstruction() {
        assertThatNullPointerException().isThrownBy(() -> new CourseOffering(null, 1, Set.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new CourseOffering("offering", 0, Set.of()));
        assertThatNullPointerException().isThrownBy(() -> new CourseOffering("offering", 1, null));
    }

    @Test
    void exposesOfferingPropertiesAndImmutablePrerequisites() {
        CourseOffering offering = new CourseOffering("offering", 2, Set.of("intro-java"));

        assertThat(offering.offeringId()).isEqualTo("offering");
        assertThat(offering.capacity()).isEqualTo(2);
        assertThat(offering.requiredPrerequisiteCourseIds()).containsExactly("intro-java");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> offering.requiredPrerequisiteCourseIds().add("another-course")
        );
    }

    @Test
    void returnsDroppedResultWithoutPromotionWhenWaitlistIsEmpty() {
        CourseOffering offering = new CourseOffering("offering", 1, Set.of());
        offering.enroll(new Learner("learner-1", Set.of()), eligibilityPolicy);

        RegistrationResult result = offering.drop("learner-1", eligibilityPolicy);

        assertThat(result.outcome()).isEqualTo(RegistrationOutcome.DROPPED);
        assertThat(result.promotedLearnerId()).isNull();
        assertThat(result.message()).contains("no eligible");
        assertThat(offering.enrolledLearnerIds()).isEmpty();
        assertThat(offering.waitlistedLearnerIds()).isEmpty();
    }

    @Test
    void retainsAllWaitlistedLearnersWhenNoOneIsEligibleForPromotion() {
        CourseOffering offering = new CourseOffering("offering", 1, Set.of());
        offering.enroll(new Learner("learner-1", Set.of()), eligibilityPolicy);
        offering.enroll(new Learner("learner-2", Set.of()), eligibilityPolicy);

        RegistrationResult result = offering.drop("learner-1", (learner, prerequisites) -> false);

        assertThat(result.promotedLearnerId()).isNull();
        assertThat(offering.waitlistedLearnerIds()).containsExactly("learner-2");
    }
}
