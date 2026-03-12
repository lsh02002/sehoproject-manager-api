package com.sehoprojectmanagerapi.repository.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByIdAndSpaceId(Long projectId, Long spaceId);
    List<Project> findFirstBySpaceIdOrderByIdAsc(Long spaceId);
}
