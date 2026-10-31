package com.example.courseregistration.domain.repository;

import com.example.courseregistration.domain.model.CourseOffering;

import java.util.Optional;

public interface CourseOfferingRepository {

    Optional<CourseOffering> findByOfferingId(String offeringId);

    void save(CourseOffering courseOffering);
}
