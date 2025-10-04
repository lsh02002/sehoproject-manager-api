package com.sehoprojectmanagerapi.web.dto.team;

import com.sehoprojectmanagerapi.repository.team.teammember.RoleTeam;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record TeamInviteResponse(
        Long id,
        Long teamId,
        Long inviterId,
        Long invitedUserId,
        String message,
        RoleTeam requestedRole,
        String status,            // PENDING / ACCEPTED / DECLINED / EXPIRED
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt
) {
}
