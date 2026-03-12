package com.sehoprojectmanagerapi.web.dto.workspace.invite;

import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import lombok.Builder;

import java.time.LocalDateTime;

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
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
