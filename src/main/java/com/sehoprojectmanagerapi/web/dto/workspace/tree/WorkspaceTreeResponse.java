package com.sehoprojectmanagerapi.web.dto.workspace.tree;

import com.sehoprojectmanagerapi.repository.common.MenuType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record WorkspaceTreeResponse(
        Long workspaceId,
        String name,
        MenuType type,
        List<SpaceNode> spaces,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record SpaceNode(Long id, String name, MenuType type, List<ProjectNode> projectNodes) {
    }

    public record ProjectNode(Long id, String name, MenuType type) {
    }
}
