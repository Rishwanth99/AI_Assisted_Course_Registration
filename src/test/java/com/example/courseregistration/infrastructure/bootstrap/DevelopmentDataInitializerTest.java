package com.example.courseregistration.infrastructure.bootstrap;

import com.example.courseregistration.infrastructure.persistence.InMemoryCourseOfferingRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentDataInitializerTest {

    @Test
    void seedsDevelopmentOffering() throws Exception {
        InMemoryCourseOfferingRepository repository = new InMemoryCourseOfferingRepository();
        DevelopmentDataInitializer initializer = new DevelopmentDataInitializer();

        initializer.seedDevelopmentOffering(repository).run();

        assertThat(repository.findByOfferingId("java-advanced-2026"))
                .hasValueSatisfying(offering -> {
                    assertThat(offering.capacity()).isEqualTo(2);
                    assertThat(offering.requiredPrerequisiteCourseIds()).containsExactly("java-fundamentals");
                });
    }
}
