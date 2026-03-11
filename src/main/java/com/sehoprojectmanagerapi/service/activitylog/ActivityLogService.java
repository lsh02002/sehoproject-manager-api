package com.sehoprojectmanagerapi.service.activitylog;

import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.ActivityLog;
import com.sehoprojectmanagerapi.repository.activity.ActivityLogRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.web.dto.activitylog.ActivityLogResponse;
import com.sehoprojectmanagerapi.web.mapper.ActivityLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @Transactional
    public void log(ActivityEntityType type, ActivityAction action, Long targetId, String message, User actor, Object beforeJson, Object afterJson) {

        String logMessage = type.toString() + "에서 " + message + " 이(가) " + action.toString() + " 되었습니다.";

        ActivityLog log = new ActivityLog(type, action, targetId, logMessage, actor, beforeJson, afterJson);

        if(Objects.equals(beforeJson, afterJson)) {
            throw new NotAcceptableException("변경된 사항이 없습니다.", null);
        }

        activityLogRepository.save(log);
    }

    @Transactional
    public List<ActivityLogResponse> getActivityLogsByUser(Long userId) {
        return activityLogRepository.findByActorId(userId)
                .stream().map(activityLogMapper::toResponse).toList();
    }
}
