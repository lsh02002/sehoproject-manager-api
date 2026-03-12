package com.sehoprojectmanagerapi.web.controller.notification;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.notification.NotificationService;
import com.sehoprojectmanagerapi.web.dto.notification.NotificationRequest;
import com.sehoprojectmanagerapi.web.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(customUserDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(customUserDetails.getId(), request));
    }

    @PostMapping("/{notificationId}")
    public ResponseEntity<?> updateNotification(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("notificationId") Long notificationId, @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.updateNotification(customUserDetails.getId(), notificationId, request));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("notificationId") Long notificationId) {
        notificationService.deleteNotification(customUserDetails.getId(), notificationId);
        return ResponseEntity.ok().build();
    }
}
