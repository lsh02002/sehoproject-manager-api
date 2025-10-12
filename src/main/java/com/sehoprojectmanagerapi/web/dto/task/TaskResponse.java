package com.sehoprojectmanagerapi.web.dto.task;

// TaskResponse.java

import com.sehoprojectmanagerapi.web.dto.tag.TagResponse;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TaskResponse(
        Long id,
        String projectKey,                 // 예: PROJ-123
        Long projectId,
        String name,
        String description,
        String state,              // OPEN/BACKLOG/IN_PROGRESS...
        String priority,
        String type,
        Integer storyPoints,
        List<AssigneeRequest> assignees,
        Long sprintId,
        Long milestoneId,
        List<TagResponse> tags,             // 여러 태그
        List<Long> dependencyIds,      // 여러 선행 작업
        LocalDate dueDate,
        java.time.LocalDateTime createdAt
) {
}
