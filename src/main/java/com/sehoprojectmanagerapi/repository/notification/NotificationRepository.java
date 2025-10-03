package com.sehoprojectmanagerapi.repository.notification;

import com.sehoprojectmanagerapi.web.dto.notification.NotificationResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverId(Long userId);
}
