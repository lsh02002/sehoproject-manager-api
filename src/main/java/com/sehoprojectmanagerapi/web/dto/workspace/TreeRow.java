package com.sehoprojectmanagerapi.web.dto.workspace;

import lombok.Builder;

@Builder
public record TreeRow(
        Long spaceId, String spaceName,
        Long projectId, String projectName
) {
}
