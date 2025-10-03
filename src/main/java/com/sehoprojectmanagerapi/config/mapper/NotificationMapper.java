package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.notification.Notification;
import com.sehoprojectmanagerapi.web.dto.notification.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getReceiver().getId(),
                n.getReceiver().getName(),
                n.getMessage(),
                n.getType().name(),
                n.getRelatedId(),
                n.isReadFlag(),
                n.getCreatedAt()
        );
    }
}
