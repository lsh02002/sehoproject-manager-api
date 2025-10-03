package com.sehoprojectmanagerapi.web.dto.milestone;

import lombok.Builder;

import java.time.LocalDate;

public record MilestoneRequest(
        Long projectId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        String status
) {}

