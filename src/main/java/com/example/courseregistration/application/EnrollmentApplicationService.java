package com.example.courseregistration.application;

import com.example.courseregistration.domain.model.CourseOffering;
import com.example.courseregistration.domain.model.Learner;
import com.example.courseregistration.domain.model.RegistrationResult;
import com.example.courseregistration.domain.policy.EligibilityPolicy;
import com.example.courseregistration.domain.repository.CourseOfferingRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EnrollmentApplicationService {

    private final CourseOfferingRepository courseOfferingRepository;
    private final EligibilityPolicy eligibilityPolicy;

    public EnrollmentApplicationService(
            CourseOfferingRepository courseOfferingRepository,
            EligibilityPolicy eligibilityPolicy
    ) {
        this.courseOfferingRepository = Objects.requireNonNull(courseOfferingRepository);
        this.eligibilityPolicy = Objects.requireNonNull(eligibilityPolicy);
    }

    public RegistrationResult enroll(String offeringId, Learner learner) {
        CourseOffering courseOffering = getCourseOffering(offeringId);
        RegistrationResult result = courseOffering.enroll(learner, eligibilityPolicy);
        courseOfferingRepository.save(courseOffering);
        return result;
    }

    public RegistrationResult drop(String offeringId, String learnerId) {
        CourseOffering courseOffering = getCourseOffering(offeringId);
        RegistrationResult result = courseOffering.drop(learnerId, eligibilityPolicy);
        courseOfferingRepository.save(courseOffering);
        return result;
    }

    public CourseOffering getCourseOffering(String offeringId) {
        return courseOfferingRepository.findByOfferingId(offeringId)
                .orElseThrow(() -> new CourseOfferingNotFoundException(offeringId));
    }
}
