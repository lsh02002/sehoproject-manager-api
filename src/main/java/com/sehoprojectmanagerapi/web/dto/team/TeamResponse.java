package com.sehoprojectmanagerapi.web.dto.team;

import lombok.Builder;

@Builder
public record TeamResponse(
        Long id,
        String name
) {
}
