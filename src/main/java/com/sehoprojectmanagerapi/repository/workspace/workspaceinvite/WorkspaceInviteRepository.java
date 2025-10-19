package com.sehoprojectmanagerapi.repository.workspace.workspaceinvite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {

    // 초대 목록
    List<WorkspaceInvite> findAllByInvitedUserId(Long invitedUserId);

    // 중복/유효 초대 체크
    boolean existsByWorkspaceIdAndInvitedUserIdAndStatusInAndExpiresAtAfter(
            Long workspaceId,
            Long invitedUserId,
            List<WorkspaceInvite.Status> statuses,
            OffsetDateTime now);

    // 수락/거절용 조회 (Fetch join 포함 버전)
    @Query("""
                SELECT i
                FROM WorkspaceInvite i
                JOIN FETCH i.workspace w
                JOIN FETCH i.invitedUser iu
                LEFT JOIN FETCH i.inviter su
                WHERE i.id = :inviteId
                  AND w.id = :workspaceId
            """)
    Optional<WorkspaceInvite> findByIdWithWorkspace(@Param("inviteId") Long inviteId,
                                                    @Param("workspaceId") Long workspaceId);

    Optional<WorkspaceInvite> findByIdAndWorkspaceId(Long inviteId, Long workspaceId);

    // 동일 사용자 다른 초대 무효화 (커스텀 @Modifying 쿼리)
    @Modifying
    @Query("""
                UPDATE WorkspaceInvite i
                SET i.status = com.sehoprojectmanagerapi.repository.workspace.workspaceinvite.WorkspaceInvite.Status.EXPIRED,
                    i.expiresAt = :now
                WHERE i.workspace.id = :workspaceId
                  AND i.invitedUser.id = :userId
                  AND i.id <> :currentInviteId
                  AND i.status = com.sehoprojectmanagerapi.repository.workspace.workspaceinvite.WorkspaceInvite.Status.PENDING
            """)
    int expireOtherPendings(@Param("workspaceId") Long workspaceId,
                            @Param("userId") Long userId,
                            @Param("currentInviteId") Long currentInviteId,
                            @Param("now") OffsetDateTime now);

    List<WorkspaceInvite> findByInviterId(Long userId);
}

