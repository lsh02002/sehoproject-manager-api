package com.sehoprojectmanagerapi.web.dto.users;

import lombok.Builder;

@Builder
public record SignupResponse(
        Long userId,
        String name
) {
}
