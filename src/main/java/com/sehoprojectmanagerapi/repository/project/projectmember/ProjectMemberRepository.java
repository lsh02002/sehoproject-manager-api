package com.sehoprojectmanagerapi.repository.project.projectmember;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    List<ProjectMember> findByUserId(Long userId);

    Optional<ProjectMember> findByUserIdAndProjectId(Long userId, Long projectId);

    boolean existsByProjectIdAndUserId(Long projectId, Long invitedUserId);

    void deleteByUserIdAndProjectId(Long userId, Long projectId);
}
