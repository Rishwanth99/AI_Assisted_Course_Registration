package com.example.courseregistration.application;

public class CourseOfferingNotFoundException extends RuntimeException {

    public CourseOfferingNotFoundException(String offeringId) {
        super("Course offering '%s' was not found.".formatted(offeringId));
    }
}
