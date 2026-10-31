package com.example.courseregistration.infrastructure.persistence;

import com.example.courseregistration.domain.model.CourseOffering;
import com.example.courseregistration.domain.repository.CourseOfferingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryCourseOfferingRepository implements CourseOfferingRepository {

    private final ConcurrentMap<String, CourseOffering> courseOfferings = new ConcurrentHashMap<>();

    @Override
    public Optional<CourseOffering> findByOfferingId(String offeringId) {
        return Optional.ofNullable(courseOfferings.get(offeringId));
    }

    @Override
    public void save(CourseOffering courseOffering) {
        courseOfferings.put(courseOffering.offeringId(), courseOffering);
    }
}
