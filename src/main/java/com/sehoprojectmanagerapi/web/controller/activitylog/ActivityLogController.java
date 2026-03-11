package com.sehoprojectmanagerapi.web.controller.activitylog;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.web.dto.activitylog.ActivityLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activitylog")
@RequiredArgsConstructor
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    @GetMapping("/user")
    public ResponseEntity<List<ActivityLogResponse>> getActivityLogsByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(activityLogService.getActivityLogsByUser(customUserDetails.getId()));
    }
}
