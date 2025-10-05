package com.sehoprojectmanagerapi.web.dto.sprint;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 스프린트 조회 응답 DTO
 */
@Builder
public record SprintResponse(
        Long id,
        Long projectId,
        String name,
        String state,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
