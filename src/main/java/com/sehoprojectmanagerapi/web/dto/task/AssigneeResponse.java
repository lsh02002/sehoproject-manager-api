package com.sehoprojectmanagerapi.web.dto.task;

import lombok.Builder;

@Builder
public record AssigneeResponse(
        Long userId,
        String username,
        String type
) {
}
