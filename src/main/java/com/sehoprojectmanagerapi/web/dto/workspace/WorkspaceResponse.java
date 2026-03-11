package com.sehoprojectmanagerapi.web.dto.workspace;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WorkspaceResponse(
        Long id,
        String name,
        String slug,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
