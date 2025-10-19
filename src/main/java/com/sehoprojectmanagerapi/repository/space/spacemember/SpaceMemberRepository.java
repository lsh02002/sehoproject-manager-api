package com.sehoprojectmanagerapi.repository.space.spacemember;

import com.sehoprojectmanagerapi.repository.common.MemberStatus;
import com.sehoprojectmanagerapi.repository.space.SpaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpaceMemberRepository extends JpaRepository<SpaceMember, Long> {

    boolean existsBySpaceIdAndUserIdAndStatus(Long spaceId, Long userId, MemberStatus status);

    Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId);

    @Query("""
                select sm.role from SpaceMember sm
                 where sm.space.id = :spaceId
                   and sm.user.id = :userId
                   and sm.status = com.sehoprojectmanagerapi.repository.common.CommonStatus.ACTIVE
            """)
    Optional<SpaceRole> findRoleBySpaceIdAndUserId(@Param("spaceId") Long spaceId,
                                                   @Param("userId") Long userId);

    @Query("""
                select sm from SpaceMember sm
                 where sm.space.id = :spaceId
                   and sm.status = com.sehoprojectmanagerapi.repository.common.CommonStatus.ACTIVE
            """)
    List<SpaceMember> findActiveMembers(@Param("spaceId") Long spaceId);

    void deleteAllBySpaceId(Long spaceId);

    @Query("""
                select count(sm) > 0
                from SpaceMember sm
                where sm.space.id = :spaceId
                  and sm.user.id = :userId
            """)
    boolean isMember(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

    boolean existsBySpaceIdAndUserId(Long spaceId, Long userId);
}
