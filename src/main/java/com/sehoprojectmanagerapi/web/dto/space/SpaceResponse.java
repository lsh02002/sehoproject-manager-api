package com.sehoprojectmanagerapi.web.dto.space;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SpaceResponse(
        Long id,
        String name,
        String slug,
        Long workspaceId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
