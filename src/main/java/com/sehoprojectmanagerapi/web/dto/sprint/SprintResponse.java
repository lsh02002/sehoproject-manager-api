package com.sehoprojectmanagerapi.web.dto.sprint;

import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

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
        List<TaskResponse> taskIds
) {
}
