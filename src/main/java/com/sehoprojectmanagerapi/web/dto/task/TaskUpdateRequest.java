package com.sehoprojectmanagerapi.web.dto.task;

import java.time.LocalDate;
import java.util.List;

/**
 * Task 수정 요청 DTO
 *
 * - null 필드: 변경 없음
 * - 빈 리스트: 해당 매핑 제거 (태그, 의존성 등)
 * - 0 이하 ID: 해제 (스프린트/마일스톤)
 */
public record TaskUpdateRequest(
        String title,
        String description,
        String priority,       // 예: "HIGH", "MEDIUM", "LOW"
        String type,           // 예: "BUG", "STORY", "TASK"
        String state,
        Integer storyPoints,
        LocalDate dueDate,

        Long sprintId,         // null: 변경 없음, 0 이하: 스프린트 해제
        Long milestoneId,      // null: 변경 없음, 0 이하: 마일스톤 해제

        List<Long> tagIds,             // null: 변경 없음, empty: 모두 제거
        List<Long> dependencyTaskIds,  // null: 변경 없음, empty: 모두 제거

        List<AssigneeRequest> assignees // null: 변경 없음
) {}
