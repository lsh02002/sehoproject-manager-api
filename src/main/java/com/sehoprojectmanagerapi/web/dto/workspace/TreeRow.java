package com.sehoprojectmanagerapi.web.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeRow {
    private Long workspaceId;
    private Long spaceId;
    private String spaceName;
    private Long projectId;
    private String projectName;
}
