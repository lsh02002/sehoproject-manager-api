package com.sehoprojectmanagerapi.web.dto.milestone;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MilestoneRequest(
        Long projectId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        String status,
        List<Long> taskIds
) {
}

