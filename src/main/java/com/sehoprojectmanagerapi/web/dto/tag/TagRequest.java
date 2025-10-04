package com.sehoprojectmanagerapi.web.dto.tag;

public record TagRequest(
        Long projectId,
        String name,
        String description
) {
}
