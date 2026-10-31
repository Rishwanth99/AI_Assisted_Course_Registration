package com.example.courseregistration.api.response;

import com.example.courseregistration.domain.model.RegistrationResult;

public record RegistrationResponse(String outcome, String message, String promotedLearnerId) {

    public static RegistrationResponse from(RegistrationResult result) {
        return new RegistrationResponse(
                result.outcome().name(),
                result.message(),
                result.promotedLearnerId()
        );
    }
}
