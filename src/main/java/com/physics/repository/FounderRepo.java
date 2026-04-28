package com.physics.repository;

import com.physics.entity.Founder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FounderRepo extends JpaRepository<Founder, Long> {
    // Since there is usually only one founder, we fetch the first one
    Optional<Founder> findFirstByOrderByIdAsc();
}