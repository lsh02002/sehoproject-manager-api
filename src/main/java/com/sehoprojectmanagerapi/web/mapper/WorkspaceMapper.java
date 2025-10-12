package com.sehoprojectmanagerapi.web.mapper;

import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.workspaceinvite.WorkspaceInvite;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceInviteResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {
    public WorkspaceResponse toResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .build();
    }

    public WorkspaceInviteResponse toInviteResponse(WorkspaceInvite invite) {
        return WorkspaceInviteResponse.builder()
                .id(invite.getId())
                .workspaceId(invite.getWorkspace().getId())
                .inviterId(invite.getInviter().getId())
                .invitedUserId(invite.getInvitedUser().getId())
                .message(invite.getMessage())
                .requestedRole(invite.getRequestedRole())
                .status(invite.getStatus().name())
                .expiresAt(invite.getExpiresAt())
                .createdAt(invite.getCreatedAt())
                .build();

    }
}
