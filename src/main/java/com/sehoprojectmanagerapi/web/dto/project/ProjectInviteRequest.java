package com.sehoprojectmanagerapi.web.dto.project;

import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;

public record ProjectInviteRequest(
        Long invitedUserId,
        String message,
        RoleProject requestedRole // null 허용
) {
}
