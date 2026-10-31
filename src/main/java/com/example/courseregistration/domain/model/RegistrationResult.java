package com.example.courseregistration.domain.model;

public record RegistrationResult(
        RegistrationOutcome outcome,
        String message,
        String promotedLearnerId
) {
    public static RegistrationResult of(RegistrationOutcome outcome, String message) {
        return new RegistrationResult(outcome, message, null);
    }

    public static RegistrationResult dropped(String promotedLearnerId) {
        String message = promotedLearnerId == null
                ? "Learner dropped; no eligible waitlisted learner was promoted."
                : "Learner dropped; waitlisted learner %s was promoted.".formatted(promotedLearnerId);
        return new RegistrationResult(RegistrationOutcome.DROPPED, message, promotedLearnerId);
    }
}
