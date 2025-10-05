package com.sehoprojectmanagerapi.repository.team.teammember;

import com.sehoprojectmanagerapi.repository.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    Optional<TeamMember> findByUserIdAndTeamId(Long userId, Long teamId);

    List<TeamMember> findByUserId(Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    void deleteByUserIdAndTeamId(Long userId, Long teamId);

    @Query("""
                SELECT COUNT(tm)
                FROM TeamMember tm
                WHERE tm.team.id = :teamId
                  AND tm.active = true
                  AND tm.user.id NOT IN (
                      SELECT pm.user.id
                      FROM ProjectMember pm
                      WHERE pm.project.id = :projectId
                  )
            """)
    long countActiveByTeamIdNotInProject(@Param("teamId") Long teamId,
                                         @Param("projectId") Long projectId);

    @Query("""
                SELECT tm.user
                FROM TeamMember tm
                WHERE tm.team.id = :teamId
                  AND tm.active = true
            """)
    List<User> findActiveUsersByTeamId(@Param("teamId") Long teamId);
}
