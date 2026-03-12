package com.sehoprojectmanagerapi.web.dto.team;

import com.sehoprojectmanagerapi.repository.team.teammember.RoleTeam;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TeamInviteResponse(
        Long id,
        Long teamId,
        Long inviterId,
        Long invitedUserId,
        String message,
        RoleTeam requestedRole,
        String status,            // PENDING / ACCEPTED / DECLINED / EXPIRED
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
