package com.sehoprojectmanagerapi.repository.project.projectinvite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectInviteRepository extends JpaRepository<ProjectInvite, Long> {
    boolean existsByProjectIdAndInvitedUserIdAndStatusInAndExpiresAtAfter(
            Long projectId, Long invitedUserId, Collection<ProjectInvite.Status> statuses, OffsetDateTime now);

    Optional<ProjectInvite> findByIdAndProjectId(Long projectInviteId, Long projectId);

    // 편의 메서드: 서비스에서 enum을 넘기지 않아도 되도록 래핑
    @Transactional
    default void expireOtherPendings(Long projectId, Long userId, Long keptInviteId, OffsetDateTime now) {
        expireOtherPendings0(
                projectId,
                userId,
                keptInviteId,
                now,
                ProjectInvite.Status.PENDING,
                ProjectInvite.Status.EXPIRED
        );
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update ProjectInvite i
               set i.status = :expired
             where i.project.id = :projectId
               and i.invitedUser.id = :userId
               and i.status = :pending
               and i.id <> :keptInviteId
               and (i.expiresAt is null or i.expiresAt > :now)
            """)
    void expireOtherPendings0(@Param("projectId") Long projectId,
                              @Param("userId") Long userId,
                              @Param("keptInviteId") Long keptInviteId,
                              @Param("now") OffsetDateTime now,
                              @Param("pending") ProjectInvite.Status pending,
                              @Param("expired") ProjectInvite.Status expired);

    List<ProjectInvite> findAllByInvitedUserId(Long invitedUserId);

    @Query("""
                select i from ProjectInvite i
                join fetch i.project p
                join fetch i.invitedUser iu
                left join fetch i.inviter su
                where i.id = :inviteId
                  and p.id = :projectId
            """)
    Optional<ProjectInvite> findByIdWithProject(@Param("inviteId") Long inviteId,
                                                @Param("projectId") Long projectId);

    @Query("""
        select i
        from ProjectInvite i
        join fetch i.project p
        left join fetch p.space s
        join fetch i.invitedUser iu
        left join fetch i.inviter su
        where i.id = :inviteId
          and p.id = :projectId
    """)
    Optional<ProjectInvite> findByIdWithProjectAndSpace(@Param("inviteId") Long inviteId,
                                                 @Param("projectId") Long projectId);
}
