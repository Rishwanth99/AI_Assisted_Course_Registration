package com.example.courseregistration.api;

import com.example.courseregistration.application.CourseOfferingNotFoundException;
import com.example.courseregistration.application.EnrollmentApplicationService;
import com.example.courseregistration.domain.model.CourseOffering;
import com.example.courseregistration.domain.model.RegistrationOutcome;
import com.example.courseregistration.domain.model.RegistrationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    private static final String OFFERING_ID = "java-advanced-2026";
    private static final String ENROLLMENT_URL = "/api/v1/course-offerings/" + OFFERING_ID + "/enrollments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentApplicationService enrollmentApplicationService;

    @Test
    void returnsBadRequestForInvalidEnrollmentRequest() throws Exception {
        mockMvc.perform(post(ENROLLMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"learnerId":"","completedCourseIds":["java-fundamentals"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void returnsCreatedForSuccessfulEnrollment() throws Exception {
        given(enrollmentApplicationService.enroll(eq(OFFERING_ID), any()))
                .willReturn(RegistrationResult.of(RegistrationOutcome.ENROLLED, "Learner enrolled successfully."));

        mockMvc.perform(post(ENROLLMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"learnerId":"learner-1","completedCourseIds":["java-fundamentals"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outcome").value("ENROLLED"));
    }

    @Test
    void returnsUnprocessableEntityForIneligibleLearner() throws Exception {
        given(enrollmentApplicationService.enroll(eq(OFFERING_ID), any()))
                .willReturn(RegistrationResult.of(RegistrationOutcome.NOT_ELIGIBLE, "Learner does not meet prerequisites."));

        mockMvc.perform(post(ENROLLMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"learnerId":"learner-1","completedCourseIds":[]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.outcome").value("NOT_ELIGIBLE"));
    }

    @Test
    void returnsAcceptedWhenEligibleLearnerIsWaitlisted() throws Exception {
        given(enrollmentApplicationService.enroll(eq(OFFERING_ID), any()))
                .willReturn(RegistrationResult.of(RegistrationOutcome.WAITLISTED, "Course is full; learner added to the waitlist."));

        mockMvc.perform(post(ENROLLMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"learnerId":"learner-3","completedCourseIds":["java-fundamentals"]}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.outcome").value("WAITLISTED"));
    }

    @Test
    void returnsNotFoundWhenDroppingLearnerWhoIsNotEnrolled() throws Exception {
        given(enrollmentApplicationService.drop(OFFERING_ID, "learner-1"))
                .willReturn(RegistrationResult.of(RegistrationOutcome.NOT_ENROLLED, "Learner is not enrolled in this offering."));

        mockMvc.perform(delete(ENROLLMENT_URL + "/learner-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.outcome").value("NOT_ENROLLED"));
    }

    @Test
    void returnsOkWhenDropSucceeds() throws Exception {
        given(enrollmentApplicationService.drop(OFFERING_ID, "learner-1"))
                .willReturn(RegistrationResult.dropped("learner-2"));

        mockMvc.perform(delete(ENROLLMENT_URL + "/learner-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("DROPPED"))
                .andExpect(jsonPath("$.promotedLearnerId").value("learner-2"));
    }

    @Test
    void returnsCurrentOfferingState() throws Exception {
        CourseOffering offering = new CourseOffering(OFFERING_ID, 2, java.util.Set.of("java-fundamentals"));
        given(enrollmentApplicationService.getCourseOffering(OFFERING_ID)).willReturn(offering);

        mockMvc.perform(get("/api/v1/course-offerings/" + OFFERING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offeringId").value(OFFERING_ID))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.availableSeats").value(2));
    }

    @Test
    void returnsNotFoundForUnknownOffering() throws Exception {
        given(enrollmentApplicationService.getCourseOffering("unknown-offering"))
                .willThrow(new CourseOfferingNotFoundException("unknown-offering"));

        mockMvc.perform(get("/api/v1/course-offerings/unknown-offering"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("OFFERING_NOT_FOUND"));
    }
}
