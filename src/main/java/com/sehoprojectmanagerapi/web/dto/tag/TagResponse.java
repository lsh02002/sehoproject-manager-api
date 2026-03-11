package com.sehoprojectmanagerapi.web.dto.tag;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TagResponse(
        Long id,
        Long projectId,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
