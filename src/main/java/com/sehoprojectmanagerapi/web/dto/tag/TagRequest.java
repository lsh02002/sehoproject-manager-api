package com.sehoprojectmanagerapi.web.dto.tag;

import lombok.Builder;

@Builder
public record TagRequest(
        Long projectId,
        String name,
        String description
) {
}
