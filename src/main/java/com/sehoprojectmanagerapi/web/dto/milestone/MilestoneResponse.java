package com.sehoprojectmanagerapi.web.dto.milestone;

import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MilestoneResponse(
        Long id,
        Long projectId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        String status,              // OPEN, IN_PROGRESS, DONE 등
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TaskResponse> taskIds
) {
}
