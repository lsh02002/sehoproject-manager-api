package com.sehoprojectmanagerapi.service.notification;

import com.sehoprojectmanagerapi.repository.notification.Notification;
import com.sehoprojectmanagerapi.repository.notification.NotificationRepository;
import com.sehoprojectmanagerapi.repository.notification.NotificationType;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.notification.NotificationRequest;
import com.sehoprojectmanagerapi.web.dto.notification.NotificationResponse;
import com.sehoprojectmanagerapi.web.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public List<NotificationResponse> getNotificationsByUserId(Long receiverId) {
        return notificationRepository.findByReceiverId(receiverId)
                .stream().map(notificationMapper::toResponse).toList();
    }

    @Transactional
    public NotificationResponse createNotification(Long userId, NotificationRequest notificationRequest) {
        User receiver = userRepository.findById(notificationRequest.receiverId())
                .orElseThrow(() -> new NotFoundException("수신자를 찾을 수 없습니다.", notificationRequest.receiverId()));

        if (notificationRequest.message().trim().isEmpty()) {
            throw new BadRequestException("메시지 란이 비어있습니다.", null);
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(notificationRequest.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("알림 형식이 잘못되어 있습니다.", null);
        }

        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setMessage(notificationRequest.message());
        notification.setType(type);
        notification.setRelatedId(notificationRequest.relatedId());

        notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationResponse updateNotification(Long userId, Long notificationId, NotificationRequest notificationRequest) {
        User receiver = userRepository.findById(notificationRequest.receiverId())
                .orElseThrow(() -> new NotFoundException("수신자를 찾을 수 없습니다.", notificationRequest.receiverId()));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("해당 알림을 찾을 수 없습니다.", notificationId));

        notification.setReceiver(receiver);

        if (!notificationRequest.message().trim().isEmpty()) {
            notification.setMessage(notificationRequest.message());
        }

        NotificationType type;
        try {
            if (notificationRequest.type() != null) {
                type = NotificationType.valueOf(notificationRequest.type().toUpperCase());
                notification.setType(type);
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("알림 형식이 잘못되어 있습니다.", null);
        }

        if (notificationRequest.relatedId() != null) {
            notification.setRelatedId(notificationRequest.relatedId());
        }

        notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
        } catch (RuntimeException e) {
            throw new NotAcceptableException("해당 알림 삭제가 잘 되지 않았습니다.", null);
        }
    }
}
