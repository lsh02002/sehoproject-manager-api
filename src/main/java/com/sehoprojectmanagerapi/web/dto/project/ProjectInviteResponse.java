package com.sehoprojectmanagerapi.web.dto.project;

import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ProjectInviteResponse(
        Long id,
        Long projectId,
        Long inviterId,
        Long invitedUserId,
        String message,
        RoleProject requestedRole,
        String status,
        OffsetDateTime expiresAt,
        java.time.LocalDateTime createdAt
) {
}
