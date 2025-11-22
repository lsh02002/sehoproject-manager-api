package com.sehoprojectmanagerapi.config.function;

import com.sehoprojectmanagerapi.repository.comment.Comment;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.taskassignee.TaskAssignee;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependency;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject.MANAGER;

@Component
public class SnapshotFunc {
    public Map<String, Object> snapshot(Object obj) {
        if (obj == null) return null;

        // Comment 객체일 경우
        if (obj instanceof Comment comment) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", comment.getId());
            m.put("taskId", comment.getTask().getId());
            m.put("projectId", comment.getTask().getProject().getId());
            m.put("authorId", comment.getAuthor() != null ? comment.getAuthor().getId() : null);
            m.put("createdAt", comment.getCreatedAt());
            m.put("body", comment.getBody() != null ? comment.getBody() : null);
            m.put("logTargetType", comment.logTargetType());
            m.put("logTargetId", comment.logTargetId());
            m.put("logMessage", comment.logMessage());
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
            m.put("milestones", project.getMilestones() != null ? project.getMilestones().stream().map(Milestone::getId).toList() : null);
            m.put("sprints", project.getSprints() != null ? project.getSprints().stream().map(Sprint::getId).toList() : null);
            m.put("tasks", project.getTasks() != null ? project.getTasks().stream().map(Task::getId).toList() : null);
            m.put("tags", project.getTags() != null ? project.getTags().stream().map(Tag::getId).toList() : null);
            m.put("createdBy", project.getCreatedBy().getId());
            m.put("spaceId", project.getSpace().getId());
            m.put("createdAt", project.getCreatedAt());
            m.put("logTargetType", project.logTargetType());
            m.put("logTargetId", project.logTargetId());
            m.put("logMessage", project.logMessage());
            return m;
        }

        if(obj instanceof ProjectMember projectMember) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", projectMember.getId());
            m.put("projectId", projectMember.getProject().getId());
            m.put("role", projectMember.getRole());
            m.put("userId", projectMember.getUser().getId());
            return m;
        }

        // Comment 객체일 경우
        if (obj instanceof Milestone milestone) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", milestone.getId());
            m.put("projectId", milestone.getProject().getId());
            m.put("name", milestone.getName());
            m.put("position", milestone.getPosition());
            m.put("description", milestone.getDescription());
            m.put("startDate", milestone.getStartDate());
            m.put("dueDate", milestone.getDueDate());
            m.put("tasks", milestone.getTasks() != null ? milestone.getTasks().stream().map(Task::getId).toList() : null);
            m.put("createdAt", milestone.getCreatedAt());
            m.put("logTargetType", milestone.logTargetType());
            m.put("logTargetId", milestone.logTargetId());
            m.put("logMessage", milestone.logMessage());
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
            m.put("tasks", sprint.getTasks() != null ? sprint.getTasks().stream().map(Task::getId).toList() : null);
            m.put("createdAt", sprint.getCreatedAt());
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
            m.put("parentId", task.getParent() != null ? task.getParent().getId() : null);
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
            m.put("closedById", task.getClosedBy() != null ? task.getClosedBy().getId() : null);
            m.put("closedAt", task.getClosedAt());
            m.put("createdAt", task.getCreatedAt());
            m.put("sprintId", task.getSprint() != null ? task.getSprint().getId() : null);
            m.put("milestoneId", task.getMilestone() != null ? task.getMilestone().getId() : null);
            m.put("tags", task.getTags() != null ? task.getTags().stream().map(Tag::getId).toList() : null);
            m.put("assignees",
                    task.getAssignees() != null
                            ? task.getAssignees().stream()
                            .map(a -> Map.of(
                                    "assigneeId", a.getAssigneeId(),
                                    "assigneeType", a.getAssigneeType()
                            ))
                            .toList()
                            : null
            );
            m.put("dependencies", task.getDependencies() != null ? task.getDependencies().stream().map(TaskDependency::getId).toList() : null);
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
