package com.sehoprojectmanagerapi.service.activitylog;

import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.ActivityLog;
import com.sehoprojectmanagerapi.repository.activity.ActivityLogRepository;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(ActivityEntityType type, ActivityAction action, Long targetId, String message, User actor, Project project) {
        ActivityLog log = new ActivityLog(type, action, targetId, message, actor, project);
        activityLogRepository.save(log);
    }
}
