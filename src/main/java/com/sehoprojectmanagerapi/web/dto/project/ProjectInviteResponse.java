package com.sehoprojectmanagerapi.web.dto.project;

import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;

import java.time.OffsetDateTime;

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
