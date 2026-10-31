package com.example.courseregistration.api.response;

import com.example.courseregistration.domain.model.CourseOffering;

import java.util.List;
import java.util.Set;

public record CourseOfferingResponse(
        String offeringId,
        int capacity,
        int enrolledCount,
        int availableSeats,
        Set<String> enrolledLearnerIds,
        List<String> waitlistedLearnerIds
) {
    public static CourseOfferingResponse from(CourseOffering courseOffering) {
        Set<String> enrolledLearnerIds = courseOffering.enrolledLearnerIds();
        return new CourseOfferingResponse(
                courseOffering.offeringId(),
                courseOffering.capacity(),
                enrolledLearnerIds.size(),
                courseOffering.capacity() - enrolledLearnerIds.size(),
                enrolledLearnerIds,
                courseOffering.waitlistedLearnerIds()
        );
    }
}
