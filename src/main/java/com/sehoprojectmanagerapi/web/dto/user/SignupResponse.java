package com.sehoprojectmanagerapi.web.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SignupResponse(
        Long userId,
        String nickname,
        Long workspaceId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
