package com.example.courseregistration.infrastructure.bootstrap;

import com.example.courseregistration.domain.model.CourseOffering;
import com.example.courseregistration.domain.repository.CourseOfferingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile("dev")
public class DevelopmentDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentDataInitializer.class);

    @Bean
    CommandLineRunner seedDevelopmentOffering(CourseOfferingRepository courseOfferingRepository) {
        return arguments -> {
            courseOfferingRepository.save(new CourseOffering("java-advanced-2026", 2, Set.of("java-fundamentals")));
            logger.info("Development offering seeded: java-advanced-2026 (capacity 2, prerequisite java-fundamentals)");
            logger.info("Try: POST /api/v1/course-offerings/java-advanced-2026/enrollments");
        };
    }
}
