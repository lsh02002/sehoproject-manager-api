package com.sehoprojectmanagerapi.web.dto.milestone;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Builder
public record MilestoneResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        String status,              // OPEN, IN_PROGRESS, DONE 등
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
