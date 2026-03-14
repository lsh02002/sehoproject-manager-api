package com.sehoprojectmanagerapi.web.dto.workspace.invite;

import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import lombok.Builder;

@Builder
public record WorkspaceInviteRequest(
        String invitedUserEmail,
        String message,
        WorkspaceRole requestedRole, // null 허용
        Long workspaceId
) {
}
