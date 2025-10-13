package com.sehoprojectmanagerapi.web.dto.sprint;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * 스프린트 생성/수정을 위한 요청 DTO
 * - 서비스에서 projectId로 권한/소속 프로젝트를 확인합니다.
 */
@Builder
public record SprintRequest(
        Long projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String state,         // 예: PLANNED, ACTIVE, CLOSED ...
        List<Long> taskIds
) {
}
