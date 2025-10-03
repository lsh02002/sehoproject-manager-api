package com.sehoprojectmanagerapi.repository.team.teammember;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    Optional<TeamMember> findByUserIdAndTeamId(Long userId, Long teamId);

    List<TeamMember> findByUserId(Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    void deleteByUserIdAndTeamId(Long userId, Long teamId);
}
