package com.sehoprojectmanagerapi.web.dto.space;

import lombok.Builder;

@Builder
public record SpaceRequest(
        Long workspaceId,
        String name,
        String slug
) {
}
