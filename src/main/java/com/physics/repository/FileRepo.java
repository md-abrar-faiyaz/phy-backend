package com.physics.repository;

import com.physics.entity.ResourceFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepo extends JpaRepository<ResourceFile, Long> {
}
