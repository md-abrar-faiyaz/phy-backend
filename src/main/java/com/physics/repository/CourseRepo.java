package com.physics.repository;

import com.physics.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepo extends JpaRepository<Course, Long> {
    List<Course> findByYear(Integer year);
}
