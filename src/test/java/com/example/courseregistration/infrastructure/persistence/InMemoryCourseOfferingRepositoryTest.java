package com.example.courseregistration.infrastructure.persistence;

import com.example.courseregistration.domain.model.CourseOffering;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCourseOfferingRepositoryTest {

    @Test
    void findsSavedOfferingAndReturnsEmptyForUnknownOffering() {
        InMemoryCourseOfferingRepository repository = new InMemoryCourseOfferingRepository();
        CourseOffering offering = new CourseOffering("offering", 1, Set.of());

        assertThat(repository.findByOfferingId("missing")).isEmpty();

        repository.save(offering);

        assertThat(repository.findByOfferingId("offering")).containsSame(offering);
    }
}
