package com.sehoprojectmanagerapi.web.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreeRow {

    private Long workspaceId;
    private Integer workspacePosition;  // 추가

    private Long spaceId;
    private String spaceName;
    private Integer spacePosition;      // 추가

    private Long projectId;
    private String projectName;
    private Integer projectPosition;   // 추가

    private Long milestoneId;
    private String milestoneName;
    private Integer milestonePosition;

    private Long sprintId;
    private String sprintName;
    private Integer sprintPosition;

    private Long taskId;             // ⬅️ 신규
    private String taskName;         // ⬅️ 신규
    private Integer taskPosition;    // ⬅️ 신규

    private boolean canEnterWorkspace;
    private boolean canEnterSpace;
    private boolean canEnterProject;
    private boolean canEnterMilestone;
    private boolean canEnterSprint;
    private boolean canEnterTask;    // ⬅️ 신규
}
