package com.sehoprojectmanagerapi.web.dto.workspace;

import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import lombok.Builder;

@Builder
public record WorkspaceInviteRequest(
        Long invitedUserId,
        String message,
        WorkspaceRole requestedRole // null 허용
) {
}
