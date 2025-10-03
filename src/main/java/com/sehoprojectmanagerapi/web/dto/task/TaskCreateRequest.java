package com.sehoprojectmanagerapi.web.dto.task;

import java.time.LocalDate;
import java.util.List;

public record TaskCreateRequest(
        Long projectId,
        String title,
        String description,
        Long assigneeId,            // null 가능
        Long sprintId,              // null 가능
        Long milestoneId,           // null 가능
        List<Long> tagIds,          // [] 가능
        List<Long> dependencyTaskIds, // [] 가능 (선행 작업들)
        String priority,            // "LOW/MEDIUM/HIGH" 등
        String type,                // "TASK/BUG/STORY" 등
        Integer storyPoints,        // null 가능
        LocalDate dueDate           // null 가능
) {
}
