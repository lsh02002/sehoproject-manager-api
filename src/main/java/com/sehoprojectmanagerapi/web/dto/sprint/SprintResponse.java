package com.sehoprojectmanagerapi.web.dto.sprint;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 스프린트 조회 응답 DTO
 */
public record SprintResponse(
        Long id,
        Long projectId,
        String name,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
