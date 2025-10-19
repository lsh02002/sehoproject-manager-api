package com.sehoprojectmanagerapi.web.dto.workspace;

import lombok.Builder;

@Builder
public record WorkspaceResponse(
        Long id,
        String name,
        String slug
) {
}
