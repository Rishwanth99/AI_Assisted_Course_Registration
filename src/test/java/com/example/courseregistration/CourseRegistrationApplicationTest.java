package com.example.courseregistration;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class CourseRegistrationApplicationTest {

    @Test
    void startsSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] arguments = {"--spring.main.web-application-type=none"};
            CourseRegistrationApplication.main(arguments);

            springApplication.verify(() -> SpringApplication.run(CourseRegistrationApplication.class, arguments));
        }
    }
}
