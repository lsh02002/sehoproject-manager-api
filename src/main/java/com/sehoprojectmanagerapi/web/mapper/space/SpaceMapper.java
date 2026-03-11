package com.sehoprojectmanagerapi.web.mapper.space;

import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import org.springframework.stereotype.Component;

@Component
public class SpaceMapper {
    public SpaceResponse toResponse(Space space) {
        return SpaceResponse.builder()
                .id(space.getId())
                .name(space.getName())
                .slug(space.getSlug())
                .workspaceId(space.getWorkspace().getId())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .build();
    }
}
