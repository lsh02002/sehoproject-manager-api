package com.sehoprojectmanagerapi.web.dto.notification;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long notificationId,
        Long receiverId,
        String receiverName,
        String message,
        String type,
        Long relatedId,
        boolean readFlag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

