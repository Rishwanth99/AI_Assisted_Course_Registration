package com.example.courseregistration.api;

import com.example.courseregistration.api.request.EnrollmentRequest;
import com.example.courseregistration.api.response.CourseOfferingResponse;
import com.example.courseregistration.api.response.RegistrationResponse;
import com.example.courseregistration.application.EnrollmentApplicationService;
import com.example.courseregistration.domain.model.Learner;
import com.example.courseregistration.domain.model.RegistrationOutcome;
import com.example.courseregistration.domain.model.RegistrationResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/course-offerings/{offeringId}")
public class EnrollmentController {

    private final EnrollmentApplicationService enrollmentApplicationService;

    public EnrollmentController(EnrollmentApplicationService enrollmentApplicationService) {
        this.enrollmentApplicationService = enrollmentApplicationService;
    }

    @PostMapping("/enrollments")
    public ResponseEntity<RegistrationResponse> enroll(
            @PathVariable @NotBlank String offeringId,
            @Valid @RequestBody EnrollmentRequest request
    ) {
        RegistrationResult result = enrollmentApplicationService.enroll(
                offeringId,
                new Learner(request.learnerId(), request.completedCourseIds())
        );
        return ResponseEntity.status(statusFor(result.outcome())).body(RegistrationResponse.from(result));
    }

    @DeleteMapping("/enrollments/{learnerId}")
    public ResponseEntity<RegistrationResponse> drop(
            @PathVariable @NotBlank String offeringId,
            @PathVariable @NotBlank String learnerId
    ) {
        RegistrationResult result = enrollmentApplicationService.drop(offeringId, learnerId);
        return ResponseEntity.status(statusFor(result.outcome())).body(RegistrationResponse.from(result));
    }

    @GetMapping
    public CourseOfferingResponse getCourseOffering(@PathVariable @NotBlank String offeringId) {
        return CourseOfferingResponse.from(enrollmentApplicationService.getCourseOffering(offeringId));
    }

    private HttpStatus statusFor(RegistrationOutcome outcome) {
        return switch (outcome) {
            case ENROLLED -> HttpStatus.CREATED;
            case WAITLISTED -> HttpStatus.ACCEPTED;
            case NOT_ELIGIBLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NOT_ENROLLED -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.OK;
        };
    }
}
