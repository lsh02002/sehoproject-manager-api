package com.sehoprojectmanagerapi.web.dto.space;

import lombok.Builder;

@Builder
public record SpaceRequest(
        String name,
        String slug
) {}
