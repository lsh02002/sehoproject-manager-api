package com.sehoprojectmanagerapi.web.dto.notification;

import lombok.Builder;

@Builder
public record NotificationResponse(
        Long notificationId,
        Long receiverId,
        String recipientName,
        String message,
        String type,
        Long relatedId,
        boolean read,
        java.time.LocalDateTime createdAt
) {
}

