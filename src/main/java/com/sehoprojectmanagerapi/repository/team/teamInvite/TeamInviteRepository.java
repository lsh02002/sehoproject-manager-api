package com.sehoprojectmanagerapi.repository.team.teamInvite;

// TeamInviteRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, Long> {

    Optional<TeamInvite> findByIdAndTeamId(Long id, Long teamId);

    boolean existsByTeamIdAndInvitedUserIdAndStatusInAndExpiresAtAfter(
            Long teamId, Long invitedUserId, Collection<TeamInvite.Status> statuses, LocalDateTime now);

    @Transactional
    default int expireOtherPendings(Long teamId, Long userId, Long keptInviteId, LocalDateTime now) {
        return expireOtherPendings0(teamId, userId, keptInviteId, now,
                TeamInvite.Status.PENDING, TeamInvite.Status.EXPIRED);
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update TeamInvite i
               set i.status = :expired
             where i.team.id = :teamId
               and i.invitedUser.id = :userId
               and i.status = :pending
               and i.id <> :keptInviteId
               and (i.expiresAt is null or i.expiresAt > :now)
            """)
    int expireOtherPendings0(@Param("teamId") Long teamId,
                             @Param("userId") Long userId,
                             @Param("keptInviteId") Long keptInviteId,
                             @Param("now") LocalDateTime now,
                             @Param("pending") TeamInvite.Status pending,
                             @Param("expired") TeamInvite.Status expired);
}
