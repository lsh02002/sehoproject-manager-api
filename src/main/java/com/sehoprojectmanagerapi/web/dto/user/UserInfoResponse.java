package com.sehoprojectmanagerapi.web.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserInfoResponse(
        Long userId,
        String nickname,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
