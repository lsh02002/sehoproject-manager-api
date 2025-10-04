package com.sehoprojectmanagerapi.web.dto.team;

import com.sehoprojectmanagerapi.repository.team.teammember.RoleTeam;
import lombok.Builder;

@Builder
public record TeamInviteRequest(
        Long invitedUserId,
        RoleTeam requestedRole,   // null이면 MEMBER 기본
        String message
) {
}
