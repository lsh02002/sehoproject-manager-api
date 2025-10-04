package com.sehoprojectmanagerapi.web.dto.tag;

import lombok.Builder;

@Builder
public record TagResponse(
        Long id,
        Long projectId,
        String name,
        String description
) {
}
