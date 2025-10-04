package com.sehoprojectmanagerapi.web.dto.team;

import lombok.Builder;

@Builder
public record TeamRequest(
        Long projectId,
        String name
) {
}
