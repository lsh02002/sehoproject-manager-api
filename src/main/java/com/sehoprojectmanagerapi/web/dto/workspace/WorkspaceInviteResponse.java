package com.sehoprojectmanagerapi.web.dto.workspace;

import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record WorkspaceInviteResponse(
        Long id,
        Long workspaceId,
        Long inviterId,
        Long invitedUserId,
        String message,
        WorkspaceRole requestedRole,
        String status,
        OffsetDateTime expiresAt,
        java.time.LocalDateTime createdAt
) {
}
