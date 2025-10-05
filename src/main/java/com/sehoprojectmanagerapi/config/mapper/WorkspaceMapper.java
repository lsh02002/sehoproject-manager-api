package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.workspace.Workspace;
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
}
