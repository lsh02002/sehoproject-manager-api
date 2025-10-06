package com.sehoprojectmanagerapi.web.dto.task;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TaskRequest(
        Long projectId,
        String title,
        String description,
        Long assigneeId,             // UserId 또는 TeamId
        String assigneeType,         // "USER" 또는 "TEAM"
        Boolean dynamicAssign,       // 팀일 때만 의미 있음 (동적 멤버 싱크)
        Long sprintId,               // null 가능
        Long milestoneId,            // null 가능
        List<Long> tagIds,           // [] 가능
        List<Long> dependencyTaskIds,// [] 가능 (선행 작업들)
        String priority,             // "LOW/MEDIUM/HIGH" 등
        String type,                 // "TASK/BUG/STORY" 등
        Integer storyPoints,         // null 가능
        LocalDate dueDate            // null 가능
) {
}
