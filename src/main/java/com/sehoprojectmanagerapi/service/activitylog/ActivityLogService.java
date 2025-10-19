package com.sehoprojectmanagerapi.service.activitylog;

import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.ActivityLog;
import com.sehoprojectmanagerapi.repository.activity.ActivityLogRepository;
import com.sehoprojectmanagerapi.repository.comment.Comment;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(ActivityEntityType type, ActivityAction action, Long targetId, String message, User actor, Project project, Object beforeJson, Object afterJson) {
        ActivityLog log = new ActivityLog(type, action, targetId, message, actor, project, snapshot(beforeJson), snapshot(afterJson));
        activityLogRepository.save(log);
    }

    private Map<String, Object> snapshot(Object obj) {
        if (obj == null) return null;

        // Comment 객체일 경우
        if (obj instanceof Comment c) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("taskId", c.getTask().getId());
            m.put("projectId", c.getTask().getProject().getId());
            m.put("authorId", c.getAuthor() != null ? c.getAuthor().getId() : null);
            m.put("createdAt", c.getCreatedAt());
            m.put("updatedAt", c.getUpdatedAt());
            m.put("body", c.getBody() != null ? c.getBody() : null);
            m.put("logTargetType", c.logTargetType());
            m.put("targetId", c.logTargetId());
            return m;
        }

        // Comment 객체일 경우
        if (obj instanceof Milestone milestone) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", milestone.getId());
            m.put("projectId", milestone.logProject().getId());
            m.put("name", milestone.getName());
            m.put("position", milestone.getPosition());
            m.put("description", milestone.getDescription());
            m.put("startDate", milestone.getStartDate());
            m.put("dueDate", milestone.getDueDate());
            m.put("createdAt", milestone.getCreatedAt());
            m.put("updatedAt", milestone.getUpdatedAt());
            m.put("logTargetType", milestone.logTargetType());
            m.put("logTargetId", milestone.logTargetId());
            m.put("logMessage", milestone.logMessage());
            return m;
        }

        if (obj instanceof Project project) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", project.getId());
            m.put("key", project.getKey());
            m.put("name", project.getName());
            m.put("position", project.getPosition());
            m.put("description", project.getDescription());
            m.put("startDate", project.getStartDate());
            m.put("dueDate", project.getDueDate());
            m.put("createdBy", project.getCreatedBy());
            m.put("spaceId", project.getSpace().getId());
            m.put("createdAt", project.getCreatedAt());
            m.put("updatedAt", project.getUpdatedAt());
            m.put("logTargetType", project.logTargetType());
            m.put("logTargetId", project.logTargetId());
            m.put("logMessage", project.logMessage());
            return m;
        }

        if (obj instanceof Sprint sprint) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", sprint.getId());
            m.put("projectId", sprint.getProject().getId());
            m.put("name", sprint.getName());
            m.put("position", sprint.getPosition());
            m.put("startDate", sprint.getStartDate());
            m.put("endDate", sprint.getEndDate());
            m.put("state", sprint.getState());
            m.put("createdAt", sprint.getCreatedAt());
            m.put("updatedAt", sprint.getUpdatedAt());
            m.put("logTargetType", sprint.logTargetType());
            m.put("logTargetId", sprint.logTargetId());
            m.put("logMessage", sprint.logMessage());
            return m;
        }

        if (obj instanceof Tag tag) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", tag.getId());
            m.put("projectId", tag.getProject().getId());
            m.put("name", tag.getName());
            m.put("description", tag.getDescription());
            m.put("logTargetType", tag.logTargetType());
            m.put("logTargetId", tag.logTargetId());
            m.put("logMessage", tag.logMessage());
            return m;
        }

        if (obj instanceof Task task) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", task.getId());
            m.put("projectId", task.getProject().getId());
            m.put("name", task.getName());
            m.put("parentId", task.getParent().getId());
            m.put("description", task.getDescription());
            m.put("type", task.getType());
            m.put("position", task.getPosition());
            m.put("state", task.getState());
            m.put("priority", task.getPriority());
            m.put("storyPoints", task.getStoryPoints());
            m.put("ordinal", task.getOrdinal());
            m.put("startDate", task.getStartDate());
            m.put("dueDate", task.getDueDate());
            m.put("createdById", task.getCreatedBy().getId());
            m.put("closedById", task.getClosedBy().getId());
            m.put("closedAt", task.getClosedAt());
            m.put("sprintId", task.getSprint().getId());
            m.put("milestoneId", task.getMilestone().getId());
            m.put("tags", task.getTags());
            m.put("logTargetType", task.logTargetType());
            m.put("logTargetId", task.logTargetId());
            m.put("logMessage", task.logMessage());
            return m;
        }



        // 다른 엔티티 타입이 들어오면 필요한 경우 여기에 추가
        // if (obj instanceof Sprint s) { ... }
        // if (obj instanceof Issue i) { ... }

        // 매칭되는 타입이 없으면 null 반환 또는 단순 toString()
        return Map.of("value", String.valueOf(obj));
    }
}
