package com.sehoprojectmanagerapi.service.activitylog;

import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.ActivityLog;
import com.sehoprojectmanagerapi.repository.activity.ActivityLogRepository;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(ActivityEntityType type, ActivityAction action, Long targetId, String message, User actor, Project project, Object beforeJson, Object afterJson) {
        ActivityLog log = new ActivityLog(type, action, targetId, message, actor, project, beforeJson, afterJson);

        if(Objects.equals(beforeJson, afterJson)) {
            throw new NotAcceptableException("변경된 사항이 없습니다.", null);
        }

        activityLogRepository.save(log);
    }
}
