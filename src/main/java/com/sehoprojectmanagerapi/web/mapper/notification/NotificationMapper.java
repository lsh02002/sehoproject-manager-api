package com.sehoprojectmanagerapi.web.mapper.notification;

import com.sehoprojectmanagerapi.repository.notification.Notification;
import com.sehoprojectmanagerapi.web.dto.notification.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getId())
                .receiverId(n.getReceiver().getId())
                .receiverName(n.getReceiver().getNickname())
                .message(n.getMessage())
                .type(n.getType().name())
                .relatedId(n.getRelatedId())
                .readFlag(n.isReadFlag())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();

    }
}
