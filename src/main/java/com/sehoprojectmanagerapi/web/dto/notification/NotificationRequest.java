package com.sehoprojectmanagerapi.web.dto.notification;

import lombok.Builder;

@Builder
public record NotificationRequest(
        Long receiverId,
        String message,
        String type,   // 예: TASK_ASSIGNED, COMMENT_ADDED, PROJECT_INVITE
        Long relatedId // 관련된 엔티티 ID (예: taskId, projectId 등)
) {}
