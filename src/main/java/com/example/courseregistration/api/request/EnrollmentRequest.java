package com.example.courseregistration.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record EnrollmentRequest(
        @NotBlank String learnerId,
        @NotNull Set<@NotBlank String> completedCourseIds
) {
}
