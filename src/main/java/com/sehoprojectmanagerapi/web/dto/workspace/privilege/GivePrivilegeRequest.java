package com.sehoprojectmanagerapi.web.dto.workspace.privilege;

import lombok.Builder;

@Builder
public record GivePrivilegeRequest(
        Long workspaceId,
        Long spaceId,
        Long projectId,
        String spaceRole,
        String roleProject
) {
}
