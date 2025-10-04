package com.sehoprojectmanagerapi.web.dto.task;

// TaskResponse.java

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TaskResponse(
        Long id,
        String key,                 // 예: PROJ-123
        Long projectId,
        String title,
        String description,
        String status,              // OPEN/BACKLOG/IN_PROGRESS...
        String priority,
        String type,
        Integer storyPoints,
        List<AssigneeResponse> assignees,
        List<Long> sprintIds,          // 여러 스프린트
        List<Long> milestoneIds,       // 여러 마일스톤
        List<Long> tagIds,             // 여러 태그
        List<Long> dependencyIds,      // 여러 선행 작업
        LocalDate dueDate,
        java.time.LocalDateTime createdAt
) {
}
