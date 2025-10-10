package com.sehoprojectmanagerapi.web.dto.user;

import lombok.Builder;

@Builder
public record UserInfoResponse(
        Long userId,
        String name,
        String email,
        String createdAt,
        String deletedAt
) {
}
