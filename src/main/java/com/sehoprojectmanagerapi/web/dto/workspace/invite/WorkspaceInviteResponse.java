package com.sehoprojectmanagerapi.web.dto.workspace.invite;

import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record WorkspaceInviteResponse(
        Long id,
        Long workspaceId,
        String workspaceName,
        String inviterEmail,
        String invitedUserEmail,
        String message,
        WorkspaceRole requestedRole,
        String status,
        OffsetDateTime expiresAt,
        String createdAt
) {
}
