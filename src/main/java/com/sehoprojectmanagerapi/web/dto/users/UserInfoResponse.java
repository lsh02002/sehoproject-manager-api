package com.sehoprojectmanagerapi.web.dto.users;

import lombok.Builder;

@Builder
public record UserInfoResponse(
        Long userId,
        String nickname,
        String email,
        String userStatus,
        String createdAt,
        String deletedAt
) {
}
