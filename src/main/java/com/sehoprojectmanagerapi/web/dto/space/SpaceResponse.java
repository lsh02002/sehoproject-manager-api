package com.sehoprojectmanagerapi.web.dto.space;

import lombok.Builder;

@Builder
public record SpaceResponse(
        Long id,
        String name,
        String slug,
        Long workspaceId
) {}
