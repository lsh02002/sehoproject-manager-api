package com.sehoprojectmanagerapi.web.dto.notification;

import lombok.Builder;

@Builder
public record NotificationResponse(
        Long notificationId,
        Long receiverId,
        String receiverName,
        String message,
        String type,
        Long relatedId,
        boolean readFlag,
        java.time.LocalDateTime createdAt
) {
}

