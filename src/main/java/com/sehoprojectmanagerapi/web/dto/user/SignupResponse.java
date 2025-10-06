package com.sehoprojectmanagerapi.web.dto.user;

import lombok.Builder;

@Builder
public record SignupResponse(
        Long userId,
        String name
) {
}
