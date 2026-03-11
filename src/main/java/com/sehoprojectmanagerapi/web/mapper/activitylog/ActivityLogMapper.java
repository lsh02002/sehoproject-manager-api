package com.sehoprojectmanagerapi.web.mapper.activitylog;

import com.sehoprojectmanagerapi.repository.activity.ActivityLog;
import com.sehoprojectmanagerapi.web.dto.activitylog.ActivityLogResponse;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {
    public ActivityLogResponse toResponse(ActivityLog activityLog) {
        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .message(activityLog.getMessage())
                .createdAt(activityLog.getCreatedAt().toString())
                .build();
    }
}
