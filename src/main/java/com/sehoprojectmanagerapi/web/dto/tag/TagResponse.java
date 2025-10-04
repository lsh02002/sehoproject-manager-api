package com.sehoprojectmanagerapi.web.dto.tag;

public record TagResponse(
        Long id,
        Long projectId,
        String name,
        String description
) {
}
