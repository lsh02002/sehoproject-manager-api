package com.sehoprojectmanagerapi.web.dto.team;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TeamResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
