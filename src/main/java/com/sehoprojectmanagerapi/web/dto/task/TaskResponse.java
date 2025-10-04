package com.sehoprojectmanagerapi.web.dto.task;

// TaskResponse.java

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TaskResponse(
        Long id,
        String projectKey,                 // 예: PROJ-123
        Long projectId,
        String title,
        String description,
        String state,              // OPEN/BACKLOG/IN_PROGRESS...
        String priority,
        String type,
        Integer storyPoints,
        List<AssigneeResponse> assignees,
        Long sprintId,
        Long milestoneId,
        List<Long> tagIds,             // 여러 태그
        List<Long> dependencyIds,      // 여러 선행 작업
        LocalDate dueDate,
        java.time.LocalDateTime createdAt
) {
}
