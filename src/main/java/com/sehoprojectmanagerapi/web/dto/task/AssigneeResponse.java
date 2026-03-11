package com.sehoprojectmanagerapi.web.dto.task;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AssigneeResponse(
        Long userId,
        String username,
        String type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
